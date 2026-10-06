# DevOps runbook (Windows)

How to run PharmaSafe through the full pipeline on one Windows laptop:
**GitHub → Jenkins → Docker → Kubernetes**.

Everything runs locally: Kubernetes comes with Docker Desktop, and Jenkins runs from a single `.war` file.

---

## 0. Prerequisites

| Tool | Check |
|---|---|
| Docker Desktop (with WSL 2) | `docker run hello-world` prints "Hello from Docker!" |
| Git | `git --version` |
| Java 21 (only needed to run Jenkins) | `java -version` shows 21. If not: `winget install EclipseAdoptium.Temurin.21.JDK` |

You do **not** need Maven installed: the Docker build downloads Maven and runs the tests inside the image build.

---

## 1. Push the code to GitHub

```bash
cd C:\Projects\college\pharmasafe
git init
git add .
git commit -m "PharmaSafe: Spring Boot app with Docker, Jenkins and Kubernetes"
git branch -M main
git remote add origin https://github.com/<your-username>/pharmasafe.git
git push -u origin main
```

Create the repository on GitHub first (**New repository → pharmasafe → Public**, no README).
Keep it **public** so Jenkins can check it out without credentials.

---

## 2. Turn on Kubernetes in Docker Desktop

1. Docker Desktop → **Settings (gear) → Kubernetes**
2. Tick **Enable Kubernetes**. If it asks for a cluster type, choose **kubeadm** (it shares Docker's local images, so no registry is needed).
3. **Apply & restart**, then wait until the bottom-left shows Kubernetes running (green). The first time takes a few minutes.
4. Check:

```powershell
kubectl config use-context docker-desktop
kubectl get nodes
```

You should see one node, `docker-desktop`, with status `Ready`.

---

## 3. Deploy by hand once (proves Docker + Kubernetes work)

```powershell
cd C:\Projects\college\pharmasafe
docker build -t pharmasafe:latest .
kubectl apply -f k8s/
kubectl get pods -w
```

- The first `docker build` takes 3–8 minutes (it downloads Maven dependencies and runs the tests).
- Wait until both pods show `1/1 Running` (press `Ctrl+C` to stop watching). The app pod may restart once while PostgreSQL is still starting. That's normal.
- Open **http://localhost:30080**.

Useful commands for the demo:

```powershell
kubectl get all                                   # everything that is running
kubectl logs deployment/pharmasafe                # app logs
kubectl scale deployment/pharmasafe --replicas=3  # scale out
kubectl get pods                                  # see 3 app pods
kubectl delete pod <one-app-pod-name>             # self-healing: Kubernetes recreates it
```

---

## 4. Set up Jenkins

### 4.1 Start Jenkins
1. Download the **Generic Java package (.war)** for the **LTS** release from **jenkins.io/download**.
2. Put it in `C:\Tools\jenkins\jenkins.war`.
3. In PowerShell (as your normal user, **not** administrator):

```powershell
cd C:\Tools\jenkins
java -jar jenkins.war --httpPort=9090
```

Keep this window open. Jenkins runs as long as it's open.
Running it as your own user means Jenkins can use your Docker Desktop and your `kubectl` settings directly.

### 4.2 First-time setup
1. Open **http://localhost:9090**.
2. Copy the initial admin password from the PowerShell window (or from `C:\Users\<you>\.jenkins\secrets\initialAdminPassword`) and paste it.
3. Click **Install suggested plugins** and wait.
4. Create your admin user, then keep the URL `http://localhost:9090/` and click **Save and Finish**.

### 4.3 Create the pipeline
1. **New Item** → name `pharmasafe` → **Pipeline** → OK.
2. Scroll to **Pipeline**:
   - Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `https://github.com/<your-username>/pharmasafe.git`
   - Branch: `*/main`
   - Script Path: `Jenkinsfile`
3. *(Optional, auto-trigger)* Under **Triggers**, tick **Poll SCM** with schedule `H/2 * * * *`, so Jenkins checks GitHub every 2 minutes and builds after each push. GitHub webhooks can't reach `localhost`, so polling is the simple option here.
4. **Save** → **Build Now**.

Open the build and the **Stages** view (or *Console Output*) to watch:
`Checkout → Build & Test (Docker) → Deploy to Kubernetes → Smoke Test`.

---

## 5. Demo script (3 minutes)

1. Show the repo on GitHub: `Dockerfile`, `Jenkinsfile`, `k8s/`.
2. Make a small visible change (e.g. a word in `src/main/resources/static/index.html`), then `git commit` and `git push`.
3. In Jenkins, click **Build Now** (or wait for polling) and show the four stages going green.
4. `kubectl get pods`: show the new pod rolling out.
5. Open **http://localhost:30080** and show the change live, then demo the app: verify `AZT-2402`, availability search, record a recall.
6. Bonus: `kubectl scale deployment/pharmasafe --replicas=3`, then delete a pod and show Kubernetes recreating it.

---

## 6. Troubleshooting

| Problem | Fix |
|---|---|
| `kubectl` not recognized in Jenkins | Close Jenkins (`Ctrl+C`), open a **new** PowerShell, start it again so it picks up the updated PATH |
| `docker` not recognized / cannot connect in Jenkins | Make sure Docker Desktop shows "Engine running", and that Jenkins was started from your user's PowerShell, not as admin or a service |
| Pod stuck in `ErrImageNeverPull` / `ImagePullBackOff` | Kubernetes can't see the local image: check Docker Desktop's Kubernetes uses **kubeadm**, then run `docker build -t pharmasafe:latest .` again |
| App pod `CrashLoopBackOff` right after first deploy | PostgreSQL was still starting. Wait 1–2 minutes; check with `kubectl logs deployment/pharmasafe` |
| Port 30080 not responding | `kubectl get svc pharmasafe` should show `8080:30080/TCP`; wait for the pod to be `1/1 Running` |
| Start completely fresh | `kubectl delete -f k8s/` then `kubectl apply -f k8s/` (add `kubectl delete pvc postgres-data` to wipe the database too) |
| Build fails in "Build & Test" | A JUnit test failed. Open the Console Output and scroll to the first `FAIL`/`ERROR` |
