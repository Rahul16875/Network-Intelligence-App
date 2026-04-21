# Network Intelligence Android App - Phased Build Plan

## Summary
The PDF describes a solid project, but it spans three scopes at once:
- a background network sensing app
- an on-device analytics engine
- a recommendation and insight product

The current repo is still a fresh Jetpack Compose starter, so development should happen in layers:
1. data collection
2. scoring
3. insights
4. recommendations
5. polish

For v1, the target scope is:
- Android app only
- on-device first
- offline-first with Room
- backend-ready data model, but no server required yet
- strong MVP suitable for portfolio, resume, and later expansion

## Phase Plan

### Phase 0 - Foundation and project setup
- Convert the starter app into a Clean Architecture structure with:
  - `presentation`
  - `domain`
  - `data`
  - `worker/monitoring`
- Add core dependencies:
  - Lifecycle ViewModel
  - Kotlin Coroutines / Flow
  - Room
  - WorkManager
  - location services
  - permissions handling
  - charts/maps library if heatmap is rendered in-app
- Define feature boundaries early:
  - monitoring
  - scoring
  - insights
  - recommendations
  - history/dashboard
- Replace the starter `MainActivity` UI with navigation shell and a dashboard placeholder.

### Phase 1 - Monitoring and raw data capture
- Build a `NetworkMonitor` that collects:
  - network type (`Wi-Fi`, `Cellular`, `No network`)
  - provider/carrier name when available
  - signal strength when available
  - coarse location or hashed location bucket
  - latency from a lightweight HTTP probe
  - recent probe failure rate as a packet-loss approximation
- Use `WorkManager` for periodic sampling instead of continuous polling.
- Store every sample locally in Room.
- Add permission handling early:
  - location permission
  - background behavior explanation
  - graceful degradation when permission is denied
- Success criterion:
  - the app can collect and persist samples over time without crashing or causing obvious battery drain.

### Phase 2 - Score engine and history UI
- Implement a `ScoreEngine` that converts each sample into a `0-100` quality score.
- Normalize score from weighted inputs:
  - latency
  - signal quality
  - failure/packet-loss proxy
  - connection type stability
- Expose historical trends via Flow from Room to ViewModel to Compose UI.
- Build the first useful screens:
  - current network status
  - latest score
  - score history chart
  - recent samples list
- Success criterion:
  - the user can open the app and immediately see a believable quality score with history.

### Phase 3 - Insight engine
- Add an `InsightEngine` that aggregates samples by:
  - time of day
  - day of week
  - location bucket
  - network/provider
- Generate product insights such as:
  - "Wi-Fi is weaker at home after 8 PM"
  - "Carrier A performs better than Carrier B in this area"
  - "Latency spikes occur during commuting hours"
- Keep insights rule-based in v1. Do not start with ML.
- Add a dedicated insights screen with confidence and supporting stats.
- Success criterion:
  - insights are reproducible, explainable, and backed by stored evidence.

### Phase 4 - Heatmap and recommendation engine
- Build local aggregation tables for area-based quality summaries.
- Render a simple heatmap or colored map markers from aggregated local stats.
- Add recommendation rules for:
  - best SIM/provider in saved locations
  - whether Wi-Fi or cellular is usually better
  - poor network zones to avoid for calls or uploads
- Recommendation output must always include the reason, not just the conclusion.
- Success criterion:
  - the app becomes a decision-support product, not only a monitoring tool.

### Phase 5 - Battery hardening, edge cases, and polish
- Tune sampling cadence to 15-30 minute intervals with constraints.
- Handle edge cases from the PDF explicitly:
  - no network
  - doze mode
  - API inconsistency across Android versions/devices
  - sudden latency spikes
- Add onboarding, permission education, empty states, and error states.
- Polish the project for demo and resume use:
  - stable dashboard
  - reliable background collection
  - screenshots
  - architecture diagram
- Success criterion:
  - the project feels complete enough to demo, explain, and present confidently.

## Core Types and Interfaces

### `NetworkSample`
- `id`
- `timestamp`
- `networkType`
- `providerName`
- `locationHash`
- `signalStrengthDbm`
- `latencyMs`
- `probeFailureRate`
- `score`

### `AggregatedStats`
- `bucketKey`
- `avgScore`
- `sampleCount`
- `bestProvider`
- `peakBadHours`

### `Insight`
- `id`
- `type`
- `title`
- `description`
- `confidence`
- `locationHash`
- `timeWindow`

### `Recommendation`
- `id`
- `category`
- `message`
- `reason`
- `expectedBenefit`

### Core domain interfaces
- `MonitorRepository`
- `SampleRepository`
- `ScoreEngine`
- `InsightEngine`
- `RecommendationEngine`

## Test Plan
- Unit tests for score calculation weights, bounds, and normalization.
- Unit tests for insight generation from fake historical datasets.
- DAO tests for Room insert/query/aggregation behavior.
- Flow/ViewModel tests using fake repositories.
- Worker tests for periodic sampling behavior and constraints.
- UI tests for dashboard, history, insights, and empty/error states.
- Manual device tests for:
  - permission denied
  - location unavailable
  - Wi-Fi to cellular switching
  - airplane mode / no network
  - doze/background restrictions

## Assumptions and Defaults
- v1 is on-device first, with no required backend.
- "Passive monitoring" means periodic constrained sampling, not constant active speed testing.
- Packet loss will be approximated through repeated lightweight probe failures, because Android limits low-level diagnostics.
- Location should be stored as coarse or hashed buckets for privacy.
- Insights and recommendations should be rule-based first, not ML-based.
- The first demo-worthy milestone is the end of Phase 2.

## Suggested Timeline
- Week 1: Phase 0
- Week 2-3: Phase 1
- Week 4: Phase 2
- Week 5: Phase 3
- Week 6: Phase 4
- Week 7-8: Phase 5
