# Viva preparation

Notes for Phanindra. Delete this file before pushing if you prefer.

Be honest if asked how it was built: *"I used an AI assistant to scaffold it; I set up and ran the pipeline myself, and I can explain each part."*

## The project

- **What does PharmaSafe do?** It helps pharmacists check whether a batch is safe to dispense (recall, quarantine, expiry, look-alike names), see who holds a recalled batch, and find safe stock at nearby pharmacies.
- **What does it NOT do?** Replace the pharmacy system, diagnose, prescribe or certify authenticity. The pharmacist makes the final decision.
- **Request flow:** browser → `BatchController` → `SafetyService` → repositories (JPA) → PostgreSQL → back as a JSON DTO.
- **Risk levels:** LOW < REVIEW_REQUIRED < HIGH_PRIORITY; the overall level is the highest alert (`RiskLevel.max`).
- **FEFO:** First-Expiry-First-Out. Availability lists the batch expiring soonest first so it gets used before it expires.

## GitHub
- **Why version control?** History of every change, collaboration through branches and pull requests, and it's the single source of truth that Jenkins builds from.
- **What triggers a build?** Jenkins polls GitHub (`Poll SCM`) and builds when a new commit appears. In a cloud setup a webhook would trigger it instantly.

## Docker
- **What is a container?** A packaged app plus everything it needs (Java runtime, libraries), so it runs the same on any machine.
- **Image vs container:** the image is the blueprint, and a container is a running instance of it.
- **Why a multi-stage Dockerfile?** Stage 1 (Maven + JDK) builds and tests the app; stage 2 copies only the `.jar` into a small JRE image. The final image is smaller and has no build tools in it.
- **Where do the tests run?** Inside `docker build` (`mvn package`). If a test fails, no image is produced.

## Jenkins
- **What is CI/CD?** Continuous Integration means every change is built and tested automatically. Continuous Delivery/Deployment means passing builds are deployed automatically.
- **Pipeline stages:** Checkout → Build & Test (Docker) → Deploy to Kubernetes → Smoke Test (curl).
- **Why `%BUILD_NUMBER%` as the image tag?** Every build gets a unique version, so you can see exactly what's deployed and roll back (`kubectl rollout undo deployment/pharmasafe`).
- **Declarative pipeline:** the pipeline is code (`Jenkinsfile`) stored in the repo, so it's version-controlled too.

## Kubernetes
- **Pod:** the smallest unit; it runs one or more containers.
- **Deployment:** keeps the desired number of pods running, recreates crashed pods, and does rolling updates.
- **Service:** a stable address for pods. `postgres` (ClusterIP) is internal only; `pharmasafe` (NodePort 30080) is reachable from the browser.
- **Secret:** stores the database username and password, injected into the app as environment variables.
- **PersistentVolumeClaim:** keeps PostgreSQL data even if the database pod restarts.
- **Readiness vs liveness probe:** readiness means "don't send traffic until ready"; liveness means "restart it if it stops responding".
- **Scaling:** `kubectl scale deployment/pharmasafe --replicas=3`.
- **Self-healing:** delete a pod and the Deployment creates a new one.

## Likely "why" questions
- **Why PostgreSQL in Kubernetes but H2 in tests?** Tests should be fast and need no setup; production-like runs use a real database.
- **Why not Spring Security yet?** It's out of scope for this version; JWT roles are on the roadmap.
- **What would you add next?** A registry (Docker Hub), a cloud Kubernetes cluster, an Ingress with HTTPS, an autoscaler, and authentication.
