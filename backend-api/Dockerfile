FROM golang:1.21-alpine AS builder
WORKDIR /app
RUN apk add --no-cache git ca-certificates
COPY go.mod go.sum ./
RUN go mod download
COPY . .
RUN CGO_ENABLED=0 GOOS=linux go build -ldflags="-s -w" -o /iondomo-backend ./cmd/server

FROM alpine:3.19
RUN addgroup -S iondomo && adduser -S iondomo -G iondomo
RUN apk add --no-cache ca-certificates
WORKDIR /app
COPY --from=builder /iondomo-backend .
RUN chown -R iondomo:iondomo /app
USER iondomo
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s CMD wget -q --spider http://localhost:8080/health || exit 1
ENTRYPOINT ["./iondomo-backend"]
