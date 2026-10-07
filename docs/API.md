# API reference

Base URL: `http://localhost:8080`. All responses are JSON.

Errors always have this shape:

```json
{ "status": 404, "error": "Not Found", "message": "No batch 'X' found. Check the number printed on the strip or carton." }
```

| Status | When |
|---|---|
| 400 | Missing parameter, invalid or unreadable body |
| 404 | Batch, medicine or recall not found |
| 409 | Batch already has an active recall |

---

## Verify a batch

`GET /api/batches/{batchNumber}/verify`. The lookup ignores case.

```json
{
  "batchNumber": "AMX-2506",
  "medicine": { "id": 2, "brandName": "Amoxivin 250", "genericName": "Amoxicillin", "strength": "250 mg", "dosageForm": "Capsule", "manufacturer": "Kavira Pharma" },
  "manufactureDate": "2026-06-05",
  "expiryDate": "2026-10-17",
  "supplier": "Charminar Pharma Supply",
  "status": "ACTIVE",
  "riskLevel": "REVIEW_REQUIRED",
  "alerts": [
    { "type": "EXPIRING_SOON", "level": "REVIEW_REQUIRED", "message": "Expires in 12 day(s) on 2026-10-17. Check the patient's course length before dispensing." },
    { "type": "LOOK_ALIKE_NAME", "level": "REVIEW_REQUIRED", "message": "Name can be confused with: Amlovin 5 (Amlodipine). Confirm the prescription before dispensing." }
  ],
  "activeRecalls": [],
  "note": "PharmaSafe assists verification. The final decision remains with the pharmacist."
}
```

Alert types: `RECALL`, `QUARANTINE`, `EXPIRED`, `EXPIRING_SOON`, `EXPIRY_NOTICE`, `LOOK_ALIKE_NAME`.

## Search medicines

`GET /api/medicines?query=amox` returns a list of medicines whose brand or generic name contains the query.

`GET /api/medicines/{id}/batches` returns the batches of that medicine: `batchNumber`, `expiryDate`, `status` and `supplier`.

## Find available stock

`GET /api/availability?medicine=azithromycin&area=LB Nagar&radiusKm=5`

| Parameter | Required | Notes |
|---|---|---|
| `medicine` | yes | Brand or generic name, partial match |
| `area` | no | One of the names from `/api/areas`. Pharmacies are measured from the area centre and sorted nearest first |
| `radiusKm` | no | Search radius in km (default 5). If nothing is inside it, the nearest 3 pharmacies with stock are returned instead |

Batches inside each pharmacy are listed earliest expiry first (FEFO).

```json
{
  "medicineQuery": "azithromycin",
  "preferredArea": "LB Nagar",
  "centerLatitude": 17.3457, "centerLongitude": 78.5522,
  "radiusKm": 5.0,
  "totalUnits": 159,
  "pharmacies": [
    { "pharmacyId": 11, "pharmacyName": "Apex Medical Store", "area": "LB Nagar", "phone": "+91 90000 00011",
      "latitude": 17.3465, "longitude": 78.551, "distanceKm": 0.2, "totalUnits": 14,
      "batches": [ { "batchNumber": "AZT-2507", "brandName": "Azithra 500", "expiryDate": "2028-08-27", "quantity": 14 } ] }
  ],
  "note": "Pharmacies within 5 km of LB Nagar, nearest first. Only active, unexpired, non-recalled batches are shown."
}
```

Without `area`, the whole network is returned, most stock first.

## Areas

`GET /api/areas` lists the 18 Hyderabad areas the search understands, each with `name`, `latitude` and `longitude`.

## Recalls

`GET /api/recalls` lists the active recalls, newest first.

`POST /api/recalls` records a recall. It returns **201** with the impact.

```json
{ "batchNumber": "OND-2503", "reason": "Particulate matter found", "severity": "CLASS_II", "issuedBy": "State Drug Control (sample)" }
```

`severity` is one of `CLASS_I`, `CLASS_II` or `CLASS_III`. `issuedBy` is optional and defaults to "Manual entry".

`GET /api/recalls/{id}/impact`

```json
{
  "recall": { "id": 1, "batchNumber": "AZT-2402", "brandName": "Azithra 500", "reason": "...", "severity": "CLASS_II", "issuedBy": "...", "issuedOn": "2026-10-02" },
  "affectedPharmacies": 3,
  "totalUnits": 88,
  "pharmacies": [ { "pharmacyName": "Lifeline Pharmacy", "area": "Kukatpally", "phone": "+91 90000 00004", "quantity": 40 }, ... ],
  "recommendedActions": [ "Notify the 3 affected pharmacies ...", "Quarantine 88 unit(s) ...", "...", "..." ]
}
```

## Dashboard

`GET /api/dashboard/summary`

```json
{ "medicines": 13, "batches": 19, "pharmacies": 6, "activeRecalls": 1, "unitsInRecalledBatches": 88, "batchesExpiringIn30Days": 1 }
```

## Sample batches to try

| Batch | What you'll see |
|---|---|
| `PCM-2501` | Safe (LOW) |
| `AZT-2402` | Recalled (HIGH_PRIORITY) |
| `CTZ-2404` | Expired (HIGH_PRIORITY) |
| `LOS-2408` | Quarantined (HIGH_PRIORITY) |
| `AMX-2506` | Expiring soon + look-alike name (REVIEW_REQUIRED) |
| `PNT-2503` | Expiry notice (LOW) |
| `MTF-2412` | Look-alike name (REVIEW_REQUIRED) |
