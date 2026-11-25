
import yfinance as yf
import pandas as pd
from pathlib import Path

# Dataset configuration
TICKERS = [
    "NVDA", "AMD", "AVGO", "QCOM", "MRVL", "ARM",
    "TSM", "INTC", "GFS", "UMC",
    "MU", "WDC", "STX", "SNDK",
    "ASML", "AMAT", "LRCX", "KLAC", "TER",
    "ENTG", "MKSI", "ACLS", "KLIC",
    "TXN", "ADI", "NXPI", "ON", "MCHP", "MPWR", "SWKS"
]

START_DATE = "2026-09-27"
END_DATE = "2026-11-25"
INTERVAL = "30m"

OUTPUT_FILE = (
    Path(__file__).resolve().parent.parent
    / "data"
    / "semiconductor_prices.csv"
)


def collect_data():
    """Download and organize historical semiconductor stock data."""

    all_data = []
    failed_tickers = []

    print("Collecting semiconductor stock data...\n")

    for ticker in sorted(TICKERS):
        print(f"Downloading {ticker}...")

        try:
            stock = yf.Ticker(ticker)

            df = stock.history(
                start=START_DATE,
                end=END_DATE,
                interval=INTERVAL,
                auto_adjust=True,
                prepost=False,
                actions=False
            )

            if df.empty:
                failed_tickers.append(ticker)
                continue

            # Keep only the required price and volume fields.
            df = df[["Open", "High", "Low", "Close", "Volume"]]
            df = df.dropna()

            df = df[
                (df["Open"] > 0)
                & (df["High"] > 0)
                & (df["Low"] > 0)
                & (df["Close"] > 0)
                & (df["Volume"] >= 0)
            ].copy()

            if df.empty:
                failed_tickers.append(ticker)
                continue

            # Convert timestamps to a common UTC format.
            df.index = df.index.tz_convert("UTC")
            df.index.name = "timestamp"
            df = df.reset_index()

            df["ticker"] = ticker

            df = df.rename(columns={
                "Open": "open",
                "High": "high",
                "Low": "low",
                "Close": "close",
                "Volume": "volume"
            })

            all_data.append(df)

            print(f"  Retrieved {len(df)} records")

        except Exception as error:
            print(f"  Error: {error}")
            failed_tickers.append(ticker)

    if failed_tickers:
        raise RuntimeError(
            f"Download incomplete. Failed tickers: {failed_tickers}"
        )

    # Combine all stocks into one dataset.
    dataset = pd.concat(all_data, ignore_index=True)

    dataset = dataset[
        ["timestamp", "ticker", "open", "high", "low", "close", "volume"]
    ]

    dataset = dataset.drop_duplicates(
        subset=["ticker", "timestamp"]
    )

    dataset = dataset.sort_values(
        by=["ticker", "timestamp"]
    ).reset_index(drop=True)

    if len(dataset) < 1000:
        raise RuntimeError("Dataset contains fewer than 1,000 records.")

    # Format timestamps for easy parsing in Java.
    dataset["timestamp"] = dataset["timestamp"].dt.strftime(
        "%Y-%m-%dT%H:%M:%SZ"
    )

    OUTPUT_FILE.parent.mkdir(parents=True, exist_ok=True)

    dataset.to_csv(OUTPUT_FILE, index=False)

    print("\n" + "=" * 50)
    print("DATA COLLECTION COMPLETE")
    print("=" * 50)
    print(f"Assets collected: {dataset['ticker'].nunique()}")
    print(f"Total records: {len(dataset):,}")
    print(f"Start timestamp: {dataset['timestamp'].min()}")
    print(f"End timestamp: {dataset['timestamp'].max()}")
    print(f"Output file: {OUTPUT_FILE}")

    print("\nRecords per ticker:")
    print(dataset.groupby("ticker").size().to_string())


if __name__ == "__main__":
    collect_data()
