package push_test

import (
	"context"
	"crypto/aes"
	"crypto/cipher"
	"crypto/ecdh"
	"crypto/ecdsa"
	"crypto/elliptic"
	"crypto/hmac"
	"crypto/rand"
	"crypto/sha256"
	"encoding/base64"
	"encoding/binary"
	"encoding/json"
	"io"
	"math/big"
	"net/http"
	"net/http/httptest"
	"strings"
	"testing"
	"time"

	"github.com/Jeudry/rosafiesta/server/internal/platform/push"
)

func TestWebPushSenderEncryptsRFC8291AndSignsVAPID(t *testing.T) {
	vapidKey, err := ecdsa.GenerateKey(elliptic.P256(), rand.Reader)
	if err != nil {
		t.Fatalf("generate vapid key: %v", err)
	}
	vapidPrivScalar, err := vapidKey.Bytes()
	if err != nil {
		t.Fatalf("export vapid raw private key: %v", err)
	}
	vapidECDH, err := vapidKey.ECDH()
	if err != nil {
		t.Fatalf("convert vapid ecdh: %v", err)
	}
	vapidPubB64 := base64.RawURLEncoding.EncodeToString(vapidECDH.PublicKey().Bytes())
	vapidPrivB64 := base64.RawURLEncoding.EncodeToString(vapidPrivScalar)

	uaPriv, err := ecdh.P256().GenerateKey(rand.Reader)
	if err != nil {
		t.Fatalf("generate browser ua key: %v", err)
	}
	authSecret := make([]byte, 16)
	_, _ = io.ReadFull(rand.Reader, authSecret)

	var capturedAuth string
	var capturedEncoding string
	var capturedBody []byte
	srv := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		capturedAuth = r.Header.Get("Authorization")
		capturedEncoding = r.Header.Get("Content-Encoding")
		capturedBody, _ = io.ReadAll(r.Body)
		w.WriteHeader(http.StatusCreated)
	}))
	defer srv.Close()

	subscriptionJSON, _ := json.Marshal(map[string]any{
		"endpoint": srv.URL + "/webpush/sub-1",
		"keys": map[string]string{
			"p256dh": base64.RawURLEncoding.EncodeToString(uaPriv.PublicKey().Bytes()),
			"auth":   base64.RawURLEncoding.EncodeToString(authSecret),
		},
	})

	webSender := push.NewWebPushSender(push.WebPushConfig{
		VAPIDPublicKey:        vapidPubB64,
		VAPIDPrivateKey:       vapidPrivB64,
		Subject:               "mailto:alertas@rosafiesta.do",
		AllowPrivateEndpoints: true,
		Clock:                 func() time.Time { return time.Unix(1700000000, 0).UTC() },
	})
	multi := push.NewMultiSender(nil, nil).WithWebPush(webSender)
	if !multi.IsConfigured() {
		t.Fatal("expected MultiSender with WebPushSender to be configured")
	}

	err = multi.SendPush(context.Background(), push.Message{
		Token:      string(subscriptionJSON),
		Platform:   "web",
		AppVariant: "personal",
		Title:      "Pago confirmado",
		Body:       "Tu abono fue aprobado.",
		Category:   "pagos",
	})
	if err != nil {
		t.Fatalf("SendPush web failed: %v", err)
	}
	if capturedEncoding != "aes128gcm" {
		t.Fatalf("expected aes128gcm Content-Encoding, got %q", capturedEncoding)
	}
	verifyVAPIDHeader(t, capturedAuth, &vapidKey.PublicKey)

	decrypted := decryptTestRFC8291(t, capturedBody, uaPriv, authSecret)
	var msgPayload map[string]string
	if err := json.Unmarshal(decrypted, &msgPayload); err != nil {
		t.Fatalf("unmarshal decrypted web push json: %v", err)
	}
	if msgPayload["title"] != "Pago confirmado" || msgPayload["category"] != "pagos" {
		t.Fatalf("unexpected decrypted payload %+v", msgPayload)
	}
}

func verifyVAPIDHeader(t *testing.T, authHeader string, pubKey *ecdsa.PublicKey) {
	t.Helper()
	if !strings.HasPrefix(authHeader, "vapid t=") {
		t.Fatalf("unexpected vapid auth header: %q", authHeader)
	}
	parts := strings.Split(strings.TrimPrefix(authHeader, "vapid t="), ", k=")
	if len(parts) != 2 {
		t.Fatalf("expected t= and k= parts, got %q", authHeader)
	}
	segments := strings.Split(parts[0], ".")
	if len(segments) != 3 {
		t.Fatalf("expected 3 JWT segments, got %q", parts[0])
	}
	sigRaw, err := base64.RawURLEncoding.DecodeString(segments[2])
	if err != nil || len(sigRaw) != 64 {
		t.Fatalf("invalid vapid signature %v len=%d", err, len(sigRaw))
	}
	digest := sha256.Sum256([]byte(segments[0] + "." + segments[1]))
	if !ecdsa.Verify(pubKey, digest[:], new(big.Int).SetBytes(sigRaw[:32]), new(big.Int).SetBytes(sigRaw[32:])) {
		t.Fatal("failed to verify VAPID ES256 signature")
	}
}

func decryptTestRFC8291(t *testing.T, body []byte, uaPriv *ecdh.PrivateKey, authSecret []byte) []byte {
	t.Helper()
	if len(body) < 22 {
		t.Fatalf("encrypted body too short: %d", len(body))
	}
	salt := body[:16]
	rs := binary.BigEndian.Uint32(body[16:20])
	if rs != 4096 {
		t.Fatalf("unexpected record size %d", rs)
	}
	idLen := int(body[20])
	asPubBytes := body[21 : 21+idLen]
	ciphertext := body[21+idLen:]

	asPub, err := ecdh.P256().NewPublicKey(asPubBytes)
	if err != nil {
		t.Fatalf("parse as pub key: %v", err)
	}
	sharedSecret, err := uaPriv.ECDH(asPub)
	if err != nil {
		t.Fatalf("ua ecdh: %v", err)
	}
	ikmInfo := append([]byte("WebPush: info\x00"), uaPriv.PublicKey().Bytes()...)
	ikmInfo = append(ikmInfo, asPubBytes...)
	prkCombine := testHKDFExtract(authSecret, sharedSecret)
	ikm := testHKDFExpand(prkCombine, ikmInfo, 32)
	prkContent := testHKDFExtract(salt, ikm)
	cek := testHKDFExpand(prkContent, []byte("Content-Encoding: aes128gcm\x00"), 16)
	nonce := testHKDFExpand(prkContent, []byte("Content-Encoding: nonce\x00"), 12)

	block, err := aes.NewCipher(cek)
	if err != nil {
		t.Fatalf("aes cipher: %v", err)
	}
	gcm, err := cipher.NewGCM(block)
	if err != nil {
		t.Fatalf("gcm: %v", err)
	}
	plainPadded, err := gcm.Open(nil, nonce, ciphertext, nil)
	if err != nil {
		t.Fatalf("decrypt aes128gcm: %v", err)
	}
	if len(plainPadded) < 1 || plainPadded[len(plainPadded)-1] != 0x02 {
		t.Fatalf("missing 0x02 delimiter byte in %x", plainPadded)
	}
	return plainPadded[:len(plainPadded)-1]
}

func testHKDFExtract(salt, ikm []byte) []byte {
	mac := hmac.New(sha256.New, salt)
	mac.Write(ikm)
	return mac.Sum(nil)
}

func testHKDFExpand(prk, info []byte, length int) []byte {
	mac := hmac.New(sha256.New, prk)
	mac.Write(info)
	mac.Write([]byte{1})
	return mac.Sum(nil)[:length]
}
