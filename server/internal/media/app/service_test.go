package app_test

import (
	"bytes"
	"context"
	"errors"
	"io"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/media/app"
	"github.com/Jeudry/rosafiesta/server/internal/media/domain"
	"github.com/Jeudry/rosafiesta/server/internal/media/postgres"
)

func TestMediaUploadDetectsPNGAndReadsBytesBack(t *testing.T) {
	store := postgres.NewMemoryStore()
	svc := app.NewService(store, t.TempDir(), 1024*1024, time.Now)
	pngBytes := []byte{0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D}

	asset, err := svc.Upload(context.Background(), domain.UploadInput{
		OwnerID:  "user-1",
		Purpose:  "voucher",
		Filename: "comprobante.png",
		Data:     pngBytes,
	})
	if err != nil {
		t.Fatalf("unexpected upload error: %v", err)
	}
	if asset.MimeType != "image/png" {
		t.Fatalf("expected image/png, got %s", asset.MimeType)
	}
	opened, data, err := svc.Open(context.Background(), asset.ID)
	if err != nil {
		t.Fatalf("unexpected open error: %v", err)
	}
	if opened.ID != asset.ID || !bytes.Equal(data, pngBytes) {
		t.Fatalf("opened asset mismatch")
	}
}

func TestMediaUploadRejectsInvalidBytesAndOversizedPayloads(t *testing.T) {
	store := postgres.NewMemoryStore()
	svc := app.NewService(store, t.TempDir(), 8, time.Now)

	_, err := svc.Upload(context.Background(), domain.UploadInput{
		Filename: "script.exe",
		Data:     []byte("MZ12"),
	})
	if !errors.Is(err, domain.ErrUnsupportedMimeType) {
		t.Fatalf("expected ErrUnsupportedMimeType, got %v", err)
	}

	_, err = svc.Upload(context.Background(), domain.UploadInput{
		Filename: "big.png",
		Data:     make([]byte, 32),
	})
	if !errors.Is(err, domain.ErrFileTooLarge) {
		t.Fatalf("expected ErrFileTooLarge, got %v", err)
	}
}

func newFakeS3MediaServer(capturedAuth *string) *httptest.Server {
	storedObjects := make(map[string][]byte)
	return httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		switch r.Method {
		case http.MethodPut:
			*capturedAuth = r.Header.Get("Authorization")
			body, _ := io.ReadAll(r.Body)
			storedObjects[r.URL.Path] = body
			w.WriteHeader(http.StatusOK)
		case http.MethodGet:
			body, exists := storedObjects[r.URL.Path]
			if !exists {
				w.WriteHeader(http.StatusNotFound)
				return
			}
			_, _ = w.Write(body)
		default:
			w.WriteHeader(http.StatusMethodNotAllowed)
		}
	}))
}

func TestMediaServiceSupportsS3SigV4StorageBackend(t *testing.T) {
	var capturedAuth string
	server := newFakeS3MediaServer(&capturedAuth)
	defer server.Close()

	store := postgres.NewMemoryStore()
	svc, err := app.NewService(store, t.TempDir(), 1024*1024, time.Now).WithS3Storage(app.S3Config{
		Endpoint:        server.URL,
		Bucket:          "rosafiesta-assets",
		Region:          "us-east-1",
		AccessKeyID:     "AKIAEXAMPLE",
		SecretAccessKey: "secret-example",
		PublicBaseURL:   "https://cdn.rosafiesta.do",
	})
	if err != nil {
		t.Fatalf("configure s3 storage: %v", err)
	}

	pngBytes := []byte{0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D}
	asset, err := svc.Upload(context.Background(), domain.UploadInput{
		OwnerID:  "user-1",
		Purpose:  "catalog",
		Filename: "s3.png",
		Data:     pngBytes,
	})
	if err != nil {
		t.Fatalf("s3 upload failed: %v", err)
	}
	if !strings.HasPrefix(capturedAuth, "AWS4-HMAC-SHA256 ") {
		t.Fatalf("expected SigV4 auth header, got %q", capturedAuth)
	}
	if !strings.HasPrefix(asset.StoragePath, "s3://rosafiesta-assets/") {
		t.Fatalf("expected s3 storage path, got %q", asset.StoragePath)
	}
	if !strings.HasPrefix(asset.PublicURL, "https://cdn.rosafiesta.do/") {
		t.Fatalf("expected CDN public URL for public catalog asset, got %q", asset.PublicURL)
	}
	privateAsset, err := svc.Upload(context.Background(), domain.UploadInput{
		OwnerID:  "user-1",
		Purpose:  "voucher",
		Filename: "voucher.png",
		Data:     pngBytes,
	})
	if err != nil {
		t.Fatalf("private s3 upload failed: %v", err)
	}
	if !strings.HasPrefix(privateAsset.PublicURL, "/v1/media/") {
		t.Fatalf("expected private asset to hide CDN public URL, got %q", privateAsset.PublicURL)
	}
	_, openedBytes, err := svc.Open(context.Background(), asset.ID)
	if err != nil {
		t.Fatalf("s3 open error: %v", err)
	}
	if !bytes.Equal(openedBytes, pngBytes) {
		t.Fatalf("s3 open payload mismatch len=%d", len(openedBytes))
	}
}

func TestMediaUploadDetectsSVGAndRejectsUnsafeScripts(t *testing.T) {
	store := postgres.NewMemoryStore()
	svc := app.NewService(store, t.TempDir(), 1024*1024, time.Now)
	svgBytes := []byte(`<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M4 12h16"/></svg>`)

	asset, err := svc.Upload(context.Background(), domain.UploadInput{
		OwnerID:  "user-1",
		Purpose:  "category-icon",
		Filename: "estrella.svg",
		MimeType: "image/svg+xml",
		Data:     svgBytes,
	})
	if err != nil {
		t.Fatalf("unexpected svg upload error: %v", err)
	}
	if asset.MimeType != "image/svg+xml" {
		t.Fatalf("expected image/svg+xml, got %s", asset.MimeType)
	}

	unsafeSVG := []byte(`<svg xmlns="http://www.w3.org/2000/svg"><script>alert(1)</script></svg>`)
	if _, unsafeErr := svc.Upload(context.Background(), domain.UploadInput{
		Filename: "evil.svg",
		Data:     unsafeSVG,
	}); !errors.Is(unsafeErr, domain.ErrUnsupportedMimeType) {
		t.Fatalf("expected ErrUnsupportedMimeType for scripted svg, got %v", unsafeErr)
	}
}
