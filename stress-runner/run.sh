#!/bin/bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://movements-service:8091}"
DATA_FILE="/app/request.csv"

until curl -s -f "$BASE_URL/actuator/health" >/dev/null 2>&1; do
  echo "⏳ Waiting for $BASE_URL to be available..."
  sleep 3
done

echo "🚀 Starting stress test against $BASE_URL"
echo "-----------------------------------------"

# Limpia caracteres de Windows (\r)
sed -i 's/\r$//' "$DATA_FILE"

# Saltar encabezado y eliminar líneas vacías
tail -n +2 "$DATA_FILE" | grep -v '^\s*$' | while IFS=',' read -r FROM TO ORIGIN DESTINATION AMOUNT; do
  # Trim (por si hay espacios)
  FROM=$(echo "$FROM" | xargs)
  TO=$(echo "$TO" | xargs)
  ORIGIN=$(echo "$ORIGIN" | xargs)
  DESTINATION=$(echo "$DESTINATION" | xargs)
  AMOUNT=$(echo "$AMOUNT" | xargs)

  if [ -z "$FROM" ] || [ -z "$TO" ] || [ -z "$AMOUNT" ]; then
    echo "⚠️  Línea vacía o mal formateada. Se omite."
    continue
  fi

  echo "➡️  from:$FROM to:$TO | Processing: $ORIGIN → $DESTINATION | amount: $AMOUNT"

  # Llamada al endpoint fx/quote
  QUOTE_RESPONSE=$(curl -s -X POST "$BASE_URL/fx/quote" \
    -H "Content-Type: application/json" \
    -d "{\"origin_currency\": \"$FROM\", \"destination_currency\": \"$TO\", \"amount\": $AMOUNT}" || true)

  echo "----->>> $QUOTE_RESPONSE"
  QUOTE_ID=$(echo "$QUOTE_RESPONSE" | jq -r '.quote_id' 2>/dev/null || echo "")

  if [[ -z "$QUOTE_ID" || "$QUOTE_ID" == "null" ]]; then
    echo "❌ Could not get quote_id. Response:"
    echo "$QUOTE_RESPONSE"
    echo "-----------------------------------------"
    continue
  fi

  echo "✅ Got quote_id: $QUOTE_ID"

  REQUEST_RESPONSE=$(curl -s -X POST "$BASE_URL/cbmm/request" \
      -H "Content-Type: application/json" \
      -d "{\"quote_id\":\"$QUOTE_ID\",\"origin_account\":\"$ORIGIN\",\"destination_account\":\"$DESTINATION\",\"amount\":$AMOUNT}" || true)

  echo "📦 Response: $REQUEST_RESPONSE"
  echo "-----------------------------------------"
done

echo "✅ Stress test finished successfully."
