# StockLedger Java Spring Boot Backend

Complete Java 21 + Spring Boot 3.4 REST API backend for the StockLedger Inventory & Order Management System.

## Run Locally
```bash
mvn spring-boot:run
```

The backend starts on `http://localhost:8080` with an embedded H2 database (`./data/stockledger`) and H2 Web Console at `http://localhost:8080/h2-console`.

## Deploy with Docker (Render / Railway / Koyeb)
```bash
docker build -t stockledger-java .
docker run -p 8080:8080 stockledger-java
```

## REST Endpoints
- `GET /api/health` — Service health & JVM version
- `GET /api/summary` — Live inventory valuation, low-stock count & revenue metrics
- `GET /api/products` — List / search / filter products
- `POST /api/products` — Create a new SKU
- `PUT /api/products/{id}` — Update SKU details or stock count
- `DELETE /api/products/{id}` — Delete SKU
- `GET /api/orders` — List orders by status
- `POST /api/orders` — Transactional order creation with automatic stock deduction
- `PATCH /api/orders/{id}/status` — Update fulfillment status (restores stock on cancel)
- `DELETE /api/orders/{id}` — Remove order record
