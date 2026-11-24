# Imagen de producción del servidor: compila en una etapa y empaqueta solo el binario estático sin shell ni usuario root.
FROM golang:1.26 AS build
WORKDIR /src
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 go build -trimpath -ldflags="-s -w" -o /out/api ./cmd/api
RUN mkdir -p /var/lib/rosafiesta/media && chown 65532:65532 /var/lib/rosafiesta/media

FROM gcr.io/distroless/static-debian12:nonroot
COPY --from=build --chown=65532:65532 /var/lib/rosafiesta/media /var/lib/rosafiesta/media
COPY --from=build /out/api /api
EXPOSE 8788
USER nonroot
HEALTHCHECK CMD ["/api", "healthcheck"]
ENTRYPOINT ["/api"]
