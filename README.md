# Banking Transaction Processor

A RESTful banking transaction service built with Java and Spring Boot. The
application supports creating accounts, checking balances, depositing and
withdrawing funds, transferring money between accounts, and viewing transaction
history.

> **Important:** Account and transaction data is stored in memory. All data is
> lost when the application stops or restarts.

## Features

- Create an account with an optional opening balance
- Retrieve an account balance
- Deposit funds
- Withdraw funds with insufficient-funds protection
- Transfer funds atomically between two accounts
- Retrieve an account's transaction history
- Validate monetary values to two decimal places
- Return consistent JSON error responses
- Generate UTC timestamps
- Serialize account operations to keep in-memory balance updates consistent

## Technology Stack

| Component | Technology |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3.5.7 |
| Web API | Spring MVC |
| Validation | Jakarta Bean Validation |
| Build tool | Maven |
| Testing | JUnit 5 / Spring Boot Test |
| Storage | In-memory `ConcurrentHashMap` |

## Project Structure

```text
banking-transaction-processor/
|-- pom.xml
|-- README.md
`-- src/
    `-- main/
        `-- java/com/example/banking/
            |-- BankingApplication.java
            |-- config/
            |   `-- ClockConfig.java
            |-- controller/
            |   |-- AccountController.java
            |   `-- TransferController.java
            |-- dto/
            |   |-- AccountResponse.java
            |   |-- AmountRequest.java
            |   |-- BalanceResponse.java
            |   |-- CreateAccountRequest.java
            |   |-- TransactionResponse.java
            |   |-- TransferRequest.java
            |   `-- TransferResponse.java
            |-- exception/
            |   |-- ApiExceptionHandler.java
            |   `-- BankingException.java
            |-- model/
            |   |-- Account.java
            |   |-- Transaction.java
            |   `-- TransactionType.java
            |-- repository/
            |   `-- InMemoryAccountRepository.java
            `-- service/
                `-- AccountService.java
```

## Architecture

The application uses a conventional layered structure:

1. **Controllers** expose the HTTP endpoints and convert requests into service
   calls.
2. **DTOs** define and validate API request and response payloads.
3. **Account service** implements account operations, monetary validation, and
   transaction creation.
4. **In-memory repository** stores accounts in a thread-safe map.
5. **Exception handler** converts validation and domain errors into consistent
   HTTP responses.
6. **Clock configuration** supplies a UTC `Clock`, keeping timestamp creation
   explicit and testable.

Mutating operations in `AccountService` are synchronized. A transfer validates
both resulting balances before changing either account, then records matching
`TRANSFER_OUT` and `TRANSFER_IN` transactions with the same transfer ID and
timestamp.

## Prerequisites

Install:

- JDK 17 or later
- Apache Maven 3.6.3 or later

Verify the installations:

```powershell
java -version
mvn -version
```

## Getting Started

### Run in development mode

From the project root:

```powershell
mvn spring-boot:run
```

The API starts on:

```text
http://localhost:8080
```

### Build and run the packaged application

```powershell
mvn clean package
java -jar target\banking-transaction-processor-0.0.1-SNAPSHOT.jar
```

To use a different port:

```powershell
java -jar target\banking-transaction-processor-0.0.1-SNAPSHOT.jar --server.port=9090
```

## API Overview

| Method | Endpoint | Description | Success status |
| --- | --- | --- | --- |
| `POST` | `/accounts` | Create an account | `201 Created` |
| `GET` | `/accounts/{id}/balance` | Get an account balance | `200 OK` |
| `POST` | `/accounts/{id}/deposit` | Deposit funds | `200 OK` |
| `POST` | `/accounts/{id}/withdraw` | Withdraw funds | `200 OK` |
| `GET` | `/accounts/{id}/transactions` | Get transaction history | `200 OK` |
| `POST` | `/transfer` | Transfer funds between accounts | `200 OK` |

All request and response bodies use JSON. Supply the following header when a
request has a body:

```text
Content-Type: application/json
```

Money values are represented as JSON numbers and returned with two decimal
places.

### Create an account

Creates an account with an optional opening balance.

```http
POST /accounts
```

Request:

```json
{
  "openingBalance": 1000.00
}
```

The request body may be omitted, `{}` may be supplied, or `openingBalance` may
be `null`. In each case, the account starts with a balance of `0.00`.

Example:

```powershell
curl.exe -X POST http://localhost:8080/accounts `
  -H "Content-Type: application/json" `
  -d '{"openingBalance":1000.00}'
```

Response (`201 Created`):

```json
{
  "accountId": "59cf4cbc-c6d4-4c86-9af6-63187e46151e",
  "balance": 1000.00,
  "createdAt": "2026-10-14T03:34:37.765Z"
}
```

### Get an account balance

```http
GET /accounts/{id}/balance
```

Example:

```powershell
curl.exe http://localhost:8080/accounts/59cf4cbc-c6d4-4c86-9af6-63187e46151e/balance
```

Response (`200 OK`):

```json
{
  "accountId": "59cf4cbc-c6d4-4c86-9af6-63187e46151e",
  "balance": 1000.00
}
```

### Deposit funds

```http
POST /accounts/{id}/deposit
```

Request:

```json
{
  "amount": 250.50
}
```

Example:

```powershell
curl.exe -X POST http://localhost:8080/accounts/59cf4cbc-c6d4-4c86-9af6-63187e46151e/deposit `
  -H "Content-Type: application/json" `
  -d '{"amount":250.50}'
```

Response (`200 OK`):

```json
{
  "transactionId": "05e0835d-9cf6-4fab-8c8b-4232567f2e85",
  "accountId": "59cf4cbc-c6d4-4c86-9af6-63187e46151e",
  "type": "DEPOSIT",
  "amount": 250.50,
  "balanceAfter": 1250.50,
  "counterpartyAccountId": null,
  "description": "Deposit",
  "timestamp": "2026-10-14T03:40:00Z"
}
```

### Withdraw funds

```http
POST /accounts/{id}/withdraw
```

Request:

```json
{
  "amount": 100.00
}
```

Example:

```powershell
curl.exe -X POST http://localhost:8080/accounts/59cf4cbc-c6d4-4c86-9af6-63187e46151e/withdraw `
  -H "Content-Type: application/json" `
  -d '{"amount":100.00}'
```

Response (`200 OK`):

```json
{
  "transactionId": "57a18806-fd13-45b7-abcf-ab781227191c",
  "accountId": "59cf4cbc-c6d4-4c86-9af6-63187e46151e",
  "type": "WITHDRAWAL",
  "amount": 100.00,
  "balanceAfter": 1150.50,
  "counterpartyAccountId": null,
  "description": "Withdrawal",
  "timestamp": "2026-10-14T03:42:00Z"
}
```

The request is rejected if the withdrawal would make the balance negative.

### Transfer funds

Transfers money from one existing account to another existing account.

```http
POST /transfer
```

Request:

```json
{
  "sourceAccountId": "59cf4cbc-c6d4-4c86-9af6-63187e46151e",
  "destinationAccountId": "d6bcfeeb-47fc-40d9-99de-ea66c87ee27c",
  "amount": 200.00
}
```

Example:

```powershell
curl.exe -X POST http://localhost:8080/transfer `
  -H "Content-Type: application/json" `
  -d '{"sourceAccountId":"59cf4cbc-c6d4-4c86-9af6-63187e46151e","destinationAccountId":"d6bcfeeb-47fc-40d9-99de-ea66c87ee27c","amount":200.00}'
```

Response (`200 OK`):

```json
{
  "transferId": "dfb8be86-0cc3-47f1-a909-9831bfbefec9",
  "debit": {
    "transactionId": "dfb8be86-0cc3-47f1-a909-9831bfbefec9",
    "accountId": "59cf4cbc-c6d4-4c86-9af6-63187e46151e",
    "type": "TRANSFER_OUT",
    "amount": 200.00,
    "balanceAfter": 950.50,
    "counterpartyAccountId": "d6bcfeeb-47fc-40d9-99de-ea66c87ee27c",
    "description": "Transfer to d6bcfeeb-47fc-40d9-99de-ea66c87ee27c",
    "timestamp": "2026-10-14T03:45:00Z"
  },
  "credit": {
    "transactionId": "dfb8be86-0cc3-47f1-a909-9831bfbefec9",
    "accountId": "d6bcfeeb-47fc-40d9-99de-ea66c87ee27c",
    "type": "TRANSFER_IN",
    "amount": 200.00,
    "balanceAfter": 200.00,
    "counterpartyAccountId": "59cf4cbc-c6d4-4c86-9af6-63187e46151e",
    "description": "Transfer from 59cf4cbc-c6d4-4c86-9af6-63187e46151e",
    "timestamp": "2026-10-14T03:45:00Z"
  },
  "amount": 200.00,
  "timestamp": "2026-10-14T03:45:00Z"
}
```

Both transaction entries share the transfer ID and timestamp. Transfers to the
same account are rejected.

### Get transaction history

Returns transactions in ascending timestamp order.

```http
GET /accounts/{id}/transactions
```

Example:

```powershell
curl.exe http://localhost:8080/accounts/59cf4cbc-c6d4-4c86-9af6-63187e46151e/transactions
```

Response (`200 OK`):

```json
[
  {
    "transactionId": "05e0835d-9cf6-4fab-8c8b-4232567f2e85",
    "accountId": "59cf4cbc-c6d4-4c86-9af6-63187e46151e",
    "type": "DEPOSIT",
    "amount": 250.50,
    "balanceAfter": 1250.50,
    "counterpartyAccountId": null,
    "description": "Deposit",
    "timestamp": "2026-10-14T03:40:00Z"
  }
]
```

Creating an account with an opening balance does not create a transaction
history entry.

## Transaction Types

| Type | Meaning |
| --- | --- |
| `DEPOSIT` | Funds deposited directly into an account |
| `WITHDRAWAL` | Funds withdrawn directly from an account |
| `TRANSFER_OUT` | Funds sent from an account |
| `TRANSFER_IN` | Funds received by an account |

## Validation and Business Rules

- Account IDs must be valid UUIDs.
- Opening balances default to `0.00`.
- Opening balances cannot be negative.
- Deposit, withdrawal, and transfer amounts must be greater than zero.
- Monetary values may have at most 17 integer digits and two fractional digits.
- The maximum supported amount or balance is
  `99999999999999999.99`.
- Withdrawals and outgoing transfers cannot exceed the source balance.
- A transfer's source and destination accounts must be different.
- Both accounts must exist before a transfer can be completed.
- Values are normalized to a scale of two decimal places.
- All timestamps are generated using a UTC clock and serialized in ISO-8601
  format.

## Error Responses

Errors use this response shape:

```json
{
  "timestamp": "2026-10-14T03:50:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Amount must be greater than zero.",
  "path": "/accounts/59cf4cbc-c6d4-4c86-9af6-63187e46151e/deposit"
}
```

| Condition | HTTP status |
| --- | --- |
| Account does not exist | `404 Not Found` |
| Invalid or malformed request body | `400 Bad Request` |
| Invalid monetary amount | `400 Bad Request` |
| Insufficient funds | `400 Bad Request` |
| Same-account transfer | `400 Bad Request` |
| Maximum balance exceeded | `400 Bad Request` |
| Unexpected server error | `500 Internal Server Error` |

Malformed JSON receives the message:

```text
Request body is invalid or malformed.
```

## Testing

Run the test suite:

```powershell
mvn test
```

Run a clean build, including tests:

```powershell
mvn clean verify
```


## Current Limitations

- Data is not persisted across application restarts.
- The application is suitable only for a single running instance; state is not
  shared between instances.
- There is no authentication or authorization.
- There are no account owners, currencies, holds, fees, or idempotency keys.
- Transaction history is not paginated.
- Transfers are internally consistent in the current process but do not use a
  database transaction.
- No OpenAPI/Swagger endpoint is currently configured.
- Operational features such as audit logging, metrics, rate limiting, and
  distributed tracing are not included.
