#!/usr/bin/env bash

set -euo pipefail
IFS=$'\n\t'

function help {
    echo ""
    echo "Syntax"
    echo "    credibility.sh [option...] [credit|debit|retrieve] [VALUE]"
    echo ""
    echo "Operations"
    echo "    credit - Adds VALUE money to the provided account."
    echo "    debit - Deducts VALUE money from the provided account."
    echo "    retrieve - Retrieves the information for the provided account."
    echo ""
    echo "Options"
    echo "    -a <account> Assign an account Id (default=sample)"
    echo "    -p <port> Use a specific port (default=8000)"
    echo ""
}

while getopts "a:p:?" opt; do
  case $opt in
    a)
        ACCOUNT=${OPTARG}
        ;;
    p)
        PORT=${OPTARG}
        ;;
    *)
        help
        exit 0
        ;;
  esac
done

shift $((OPTIND-1))

ACCOUNT=${ACCOUNT:-sample}
PORT=${PORT:-8000}
OPERATION=${1:-retrieve}
VALUE=${2:-0}

function credit {
    echo "CREDIT $VALUE TO $ACCOUNT ON PORT $PORT"
    echo "+ curl -w '\n' -X POST http://localhost:$PORT/credibility/$ACCOUNT/credit/$VALUE"
    curl -w "\n" -X POST http://localhost:$PORT/credibility/$ACCOUNT/credit/$VALUE
}

function debit {
    echo "DEBIT $VALUE FROM $ACCOUNT ON PORT $PORT"
    echo "+ curl -w '\n' -X POST http://localhost:$PORT/credibility/$ACCOUNT/debit/$VALUE"
    curl -w "\n" -X POST http://localhost:$PORT/credibility/$ACCOUNT/debit/$VALUE
}

function retrieve {
    echo "RETRIEVE $ACCOUNT ON PORT $PORT"
    echo "+ curl -w '\n' -X GET http://localhost:$PORT/credibility/$ACCOUNT"
    curl -w "\n" -X GET http://localhost:$PORT/credibility/$ACCOUNT
}

case $OPERATION in
    credit)
        credit
        ;;
    debit)
        debit
        ;;
    retrieve)
        retrieve
        ;;
    *)
        echo "Invalid Operation"
        help
        ;;
esac
