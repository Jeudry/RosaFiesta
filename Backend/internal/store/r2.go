package store

import (
	"bytes"
	"context"
	"fmt"
	"image"
	"image/jpeg"
	"image/png"
	"io"
	"time"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/credentials"
	"github.com/aws/aws-sdk-go-v2/service/s3"
	"github.com/google/uuid"
)

const maxImageDimension = 1920
const jpegQuality = 85

// R2Config holds Cloudflare R2 configuration.
type R2Config struct {
	AccountID string
	AccessKey string
	SecretKey string
	Bucket    string
}

// R2Client wraps the S3-compatible R2 client.
type R2Client struct {
	client *s3.Client
	bucket string
}

// NewR2Client creates a new R2/S3-compatible client.
func NewR2Client(cfg R2Config) (*R2Client, error) {
	customResolver := aws.EndpointResolverWithOptionsFunc(
		func(service, region string, options ...interface{}) (aws.Endpoint, error) {
			return aws.Endpoint{
				URL:               fmt.Sprintf("https://%s.r2.cloudflarestorage.com", cfg.AccountID),
				HostnameImmutable: true,
				Source:            aws.EndpointSourceCustom,
			}, nil
		})

	awsCfg, err := config.LoadDefaultConfig(context.Background(),
		config.WithRegion("auto"),
		config.WithCredentialsProvider(credentials.NewStaticCredentialsProvider(cfg.AccessKey, cfg.SecretKey, "")),
		config.WithEndpointResolverWithOptions(customResolver),
	)
	if err != nil {
		return nil, err
	}

	client := s3.NewFromConfig(awsCfg, func(o *s3.Options) {
		o.UsePathStyle = true
	})

	return &R2Client{client: client, bucket: cfg.Bucket}, nil
}

// UploadPhoto uploads a file to R2 and returns the public URL.
func (r *R2Client) UploadPhoto(ctx context.Context, eventID uuid.UUID, filename string, contentType string, body io.Reader) (string, error) {
	key := fmt.Sprintf("events/%s/%s-%s", eventID.String(), uuid.New().String(), filename)

	_, err := r.client.PutObject(ctx, &s3.PutObjectInput{
		Bucket:      aws.String(r.bucket),
		Key:         aws.String(key),
		Body:        body,
		ContentType: aws.String(contentType),
	})
	if err != nil {
		return "", err
	}

	// R2 public URL format
	url := fmt.Sprintf("https://pub.%s.r2.cloudflarestorage.com/%s/%s", "rosafiesta", r.bucket, key)
	return url, nil
}

// GetPhoto returns a presigned URL valid for the given duration.
func (r *R2Client) GetPhoto(ctx context.Context, key string, expiry time.Duration) (string, error) {
	presignClient := s3.NewPresignClient(r.client)
	request, err := presignClient.PresignGetObject(ctx, &s3.GetObjectInput{
		Bucket: aws.String(r.bucket),
		Key:    aws.String(key),
	}, s3.WithPresignExpires(expiry))
	if err != nil {
		return "", err
	}
	return request.URL, nil
}

// DeletePhoto deletes a photo from R2.
func (r *R2Client) DeletePhoto(ctx context.Context, key string) error {
	_, err := r.client.DeleteObject(ctx, &s3.DeleteObjectInput{
		Bucket: aws.String(r.bucket),
		Key:    aws.String(key),
	})
	return err
}

// UploadFromBytes is a helper that uploads raw bytes. Images are
// compressed automatically before upload if they are JPEG/PNG and
// exceed the 1920px max dimension threshold.
func (r *R2Client) UploadFromBytes(ctx context.Context, eventID uuid.UUID, filename string, contentType string, data []byte) (string, error) {
	compressed, finalType := compressImage(data, contentType)
	return r.UploadPhoto(ctx, eventID, filename, finalType, bytes.NewReader(compressed))
}

// compressImage resizes and recompresses JPEG/PNG images that exceed
// maxImageDimension on their longest side. Returns the original data
// unchanged for non-image content types or small images.
func compressImage(data []byte, contentType string) ([]byte, string) {
	switch contentType {
	case "image/jpeg", "image/png", "image/jpg":
		// Decode config only (no full decode) to get dimensions
		cfg, err := getImageConfig(data, contentType)
		if err != nil {
			return data, contentType
		}

		longest := cfg.Width
		if cfg.Height > longest {
			longest = cfg.Height
		}

		if longest <= maxImageDimension {
			return data, contentType // No resize needed
		}

		// Full decode
		img, err := decodeImage(data, contentType)
		if err != nil {
			return data, contentType
		}

		// Calculate new dimensions maintaining aspect ratio
		var newWidth, newHeight int
		if cfg.Width >= cfg.Height {
			newWidth = maxImageDimension
			newHeight = (cfg.Height * maxImageDimension) / cfg.Width
		} else {
			newHeight = maxImageDimension
			newWidth = (cfg.Width * maxImageDimension) / cfg.Height
		}

		resized := resizeImage(img, newWidth, newHeight)

		// Encode as JPEG
		var buf bytes.Buffer
		if err := jpeg.Encode(&buf, resized, &jpeg.Options{Quality: jpegQuality}); err != nil {
			return data, contentType
		}
		return buf.Bytes(), "image/jpeg"

	default:
		return data, contentType
	}
}

func getImageConfig(data []byte, contentType string) (image.Config, error) {
	switch contentType {
	case "image/jpeg", "image/jpg":
		return jpeg.DecodeConfig(bytes.NewReader(data))
	case "image/png":
		return png.DecodeConfig(bytes.NewReader(data))
	default:
		return image.Config{}, image.ErrFormat
	}
}

func decodeImage(data []byte, contentType string) (image.Image, error) {
	switch contentType {
	case "image/jpeg", "image/jpg":
		return jpeg.Decode(bytes.NewReader(data))
	case "image/png":
		return png.Decode(bytes.NewReader(data))
	default:
		return nil, image.ErrFormat
	}
}

// resizeImage resizes img to targetWidth x targetHeight using nearest-neighbor.
func resizeImage(img image.Image, targetWidth, targetHeight int) *image.NRGBA {
	src := img.Bounds()
	srcW := src.Dx()
	srcH := src.Dy()
	offsetX := src.Min.X
	offsetY := src.Min.Y

	dst := image.NewNRGBA(image.Rect(0, 0, targetWidth, targetHeight))
	dx := float64(srcW) / float64(targetWidth)
	dy := float64(srcH) / float64(targetHeight)

	for y := 0; y < targetHeight; y++ {
		for x := 0; x < targetWidth; x++ {
			srcX := int(float64(x) * dx)
			srcY := int(float64(y) * dy)
			if srcX >= srcW {
				srcX = srcW - 1
			}
			if srcY >= srcH {
				srcY = srcH - 1
			}
			dst.Set(x, y, img.At(offsetX+srcX, offsetY+srcY))
		}
	}
	return dst
}
