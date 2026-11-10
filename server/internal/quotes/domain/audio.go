package domain

import (
	"encoding/base64"
	"math"
	"strings"
)

const (
	defaultSampleRateHz = 16000
	defaultBitsSample   = 16
	maxPCMScanSamples   = 480000
)

type AudioAnalysis struct {
	DurationMs    int
	SampleRateHz  int
	Channels      int
	BitsPerSample int
	RMSEnergy     float64
	VoiceDetected bool
	RawBytes      []byte
}

type TranscriptionResult struct {
	Transcript    string  `json:"transcript"`
	Locale        string  `json:"locale"`
	DurationMs    int     `json:"durationMs"`
	SampleRateHz  int     `json:"sampleRateHz"`
	RMSEnergy     float64 `json:"rmsEnergy"`
	VoiceDetected bool    `json:"voiceDetected"`
	Provider      string  `json:"provider"`
}

func AnalyzeBase64Audio(audioBase64 string) (AudioAnalysis, error) {
	cleaned := stripDataURLPrefix(audioBase64)
	if cleaned == "" {
		return AudioAnalysis{}, ErrInvalidAudioPayload
	}
	rawBytes, err := decodeBase64Flexible(cleaned)
	if err != nil {
		return AudioAnalysis{}, ErrInvalidAudioPayload
	}
	if len(rawBytes) < 16 {
		return AudioAnalysis{}, ErrInvalidAudioPayload
	}
	if isRIFFWaveHeader(rawBytes) {
		return analyzeWAVBytes(rawBytes)
	}
	return analyzeRawPCMBytes(rawBytes)
}

func stripDataURLPrefix(raw string) string {
	trimmed := strings.TrimSpace(raw)
	commaIdx := strings.IndexByte(trimmed, ',')
	if commaIdx > 0 {
		prefix := trimmed[:commaIdx]
		if strings.Contains(prefix, "base64") {
			afterComma := trimmed[commaIdx+1:]
			return strings.TrimSpace(afterComma)
		}
	}
	return trimmed
}

func decodeBase64Flexible(encoded string) ([]byte, error) {
	decoded, err := base64.StdEncoding.DecodeString(encoded)
	if err == nil {
		return decoded, nil
	}
	return base64.RawStdEncoding.DecodeString(encoded)
}

func isRIFFWaveHeader(raw []byte) bool {
	if len(raw) < 44 {
		return false
	}
	if string(raw[0:4]) != "RIFF" {
		return false
	}
	return string(raw[8:12]) == "WAVE"
}

func analyzeWAVBytes(raw []byte) (AudioAnalysis, error) {
	channels := readU16LE(raw[22:24])
	if channels <= 0 {
		channels = 1
	}
	sampleRate := readPositive31LE(raw[24:28])
	if sampleRate <= 0 {
		sampleRate = defaultSampleRateHz
	}
	bits := readU16LE(raw[34:36])
	if bits <= 0 {
		bits = defaultBitsSample
	}
	pcmOffset := findWAVDataOffset(raw)
	pcmSlice := raw[pcmOffset:]
	bytesPerSec := sampleRate * channels * (bits / 8)
	durationMs := 0
	if bytesPerSec > 0 {
		durationMs = (len(pcmSlice) * 1000) / bytesPerSec
	}
	rms := computePCM16RMS(pcmSlice)
	return AudioAnalysis{
		DurationMs:    durationMs,
		SampleRateHz:  sampleRate,
		Channels:      channels,
		BitsPerSample: bits,
		RMSEnergy:     rms,
		VoiceDetected: rms >= 0.008,
		RawBytes:      raw,
	}, nil
}

func findWAVDataOffset(raw []byte) int {
	offset := 12
	for chunkStep := 0; chunkStep < 16; chunkStep++ {
		if offset+8 > len(raw) {
			break
		}
		chunkID := string(raw[offset : offset+4])
		chunkSize := readPositive31LE(raw[offset+4 : offset+8])
		if chunkID == "data" {
			return offset + 8
		}
		if chunkSize <= 0 {
			break
		}
		offset += 8 + chunkSize
	}
	if len(raw) >= 44 {
		return 44
	}
	return len(raw)
}

func analyzeRawPCMBytes(raw []byte) (AudioAnalysis, error) {
	bytesPerSec := defaultSampleRateHz * 2
	durationMs := (len(raw) * 1000) / bytesPerSec
	rms := computePCM16RMS(raw)
	return AudioAnalysis{
		DurationMs:    durationMs,
		SampleRateHz:  defaultSampleRateHz,
		Channels:      1,
		BitsPerSample: defaultBitsSample,
		RMSEnergy:     rms,
		VoiceDetected: rms >= 0.008,
		RawBytes:      raw,
	}, nil
}

func computePCM16RMS(pcm []byte) float64 {
	totalSamples := len(pcm) / 2
	if totalSamples <= 0 {
		return 0
	}
	scanCount := totalSamples
	if scanCount > maxPCMScanSamples {
		scanCount = maxPCMScanSamples
	}
	var sumSquares float64
	for sampleIdx := 0; sampleIdx < scanCount; sampleIdx++ {
		byteIdx := sampleIdx * 2
		sampleVal := readS16LE(pcm[byteIdx : byteIdx+2])
		normalized := float64(sampleVal) / 32768.0
		sumSquares += normalized * normalized
	}
	meanSq := sumSquares / float64(scanCount)
	rms := math.Sqrt(meanSq)
	rounded := math.Round(rms*10000) / 10000
	return rounded
}

func readU16LE(b []byte) int {
	return int(b[0]) | (int(b[1]) << 8)
}

func readS16LE(b []byte) int {
	rawVal := readU16LE(b)
	if rawVal >= 32768 {
		return rawVal - 65536
	}
	return rawVal
}

func readPositive31LE(b []byte) int {
	b0 := int(b[0])
	b1 := int(b[1]) << 8
	b2 := int(b[2]) << 16
	b3 := int(b[3]&0x7F) << 24
	return b0 | b1 | b2 | b3
}
