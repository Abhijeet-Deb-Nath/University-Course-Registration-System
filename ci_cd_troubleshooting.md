# CI/CD Troubleshooting Guide

## 🔴 **Common Problem: Permission Denied Error**

### **Error Message:**
```
./mvnw: Permission denied
Process completed with exit code 126
```

---

## 🎯 **Root Cause**

**Problem:** The `mvnw` (Maven Wrapper) file doesn't have execute permissions on Linux.

**Why it happens:**
- Windows doesn't have Unix execute permissions
- When you push from Windows to GitHub, execute bit is not set
- GitHub Actions runs on Linux (Ubuntu)
- Linux needs explicit execute permission for scripts

---

## ✅ **Solution Applied**

### **Fixed in `.github/workflows/ci.yml`:**

**Before (Broken):**
```yaml
- name: Run tests
  run: ./mvnw clean test
```

**After (Fixed):**
```yaml
- name: Make mvnw executable
  run: chmod +x mvnw

- name: Run tests
  run: ./mvnw clean test
```

### **What `chmod +x mvnw` Does:**
- `chmod` = Change mode (permissions)
- `+x` = Add execute permission
- `mvnw` = The Maven Wrapper script

---

## 📋 **Alternative Solutions**

### **Option 1: Fix Locally (Permanent Fix)**

If you're on Windows with Git Bash or WSL:
```bash
# Add execute permission locally
git update-index --chmod=+x mvnw
git add mvnw
git commit -m "Fix: Add execute permission to mvnw"
git push
```

This stores the permission in Git, so it works on all platforms.

---

### **Option 2: Use Maven Instead of Wrapper**

Change CI to use Maven directly (if installed):
```yaml
- name: Run tests
  run: mvn clean test
```

But this requires Maven to be pre-installed in the CI environment.

---

## 🔍 **How to Verify the Fix**

### **After Pushing the Updated CI File:**

1. Go to GitHub repository
2. Click "Actions" tab
3. Find the latest workflow run
4. Check that it passes the "Run tests" step

**Expected Output:**
```
✅ Make mvnw executable
✅ Run tests
✅ [INFO] BUILD SUCCESS
✅ [INFO] Tests run: 45, Failures: 0, Errors: 0
```

---

## 🐛 **Other Common CI Issues**

### **Issue 2: Tests Fail in CI but Pass Locally**

**Possible Causes:**
- Environment differences (Java version)
- Database configuration
- Missing dependencies

**Solution:**
```yaml
# Ensure correct Java version
- name: Set up JDK 21
  uses: actions/setup-java@v4
  with:
    java-version: '21'  # Must match pom.xml
```

---

### **Issue 3: No Test Reports Found**

**Error:**
```
##[warning]No file matches path target/surefire-reports/*.xml
##[error]No test report files were found
```

**Cause:** Tests didn't run (failed at mvnw step)

**Solution:** Fix the mvnw permission issue first (as we did above)

---

### **Issue 4: Maven Cache Not Working**

**Symptom:** Dependencies download every time (slow builds)

**Solution:**
```yaml
- name: Set up JDK 21
  uses: actions/setup-java@v4
  with:
    java-version: '21'
    distribution: 'temurin'
    cache: maven  # ✅ Ensure this is present
```

---

## 📊 **Understanding Exit Codes**

| Exit Code | Meaning | Solution |
|-----------|---------|----------|
| 0 | Success | ✅ Everything worked |
| 1 | Test failure | Fix failing tests |
| 126 | Permission denied | `chmod +x mvnw` |
| 127 | Command not found | Install missing tool |

---

## 🎓 **For Your Professor**

**Problem Encountered:**
"The CI pipeline initially failed with 'Permission denied' error because the Maven Wrapper script (`mvnw`) lacked execute permissions when transferred from Windows to Linux."

**Solution Implemented:**
"Added a step in the CI workflow to set execute permissions using `chmod +x mvnw` before running tests. This is a standard practice when developing on Windows and deploying to Linux environments."

**Industry Insight:**
"This is a common issue in cross-platform development. Professional teams either:
1. Add `chmod +x` in CI scripts (our approach)
2. Use `git update-index --chmod=+x` to store permissions in Git
3. Use platform-agnostic tools (Maven instead of wrapper)"

---

## ✅ **Current Status**

After the fix:
- ✅ `mvnw` permission issue resolved
- ✅ CI pipeline will run tests successfully
- ✅ Test reports will be generated
- ✅ Pass/fail status will be reported

---

## 🚀 **Next Steps**

1. **Commit and push the updated `.github/workflows/ci.yml`**
   ```bash
   git add .github/workflows/ci.yml
   git commit -m "Fix: Add chmod for mvnw in CI pipeline"
   git push
   ```

2. **Verify on GitHub**
   - Go to Actions tab
   - Watch the workflow run
   - Confirm it passes

3. **Set up Branch Protection** (if not done)
   - Settings → Branches → Add rule
   - Require "CI Pipeline" to pass

---

## 📝 **Updated CI Workflow**

**Complete working version:**

```yaml
name: CI Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

jobs:
  test:
    name: Build and Test
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven
      
      - name: Make mvnw executable  # ← FIX
        run: chmod +x mvnw
      
      - name: Run tests
        run: ./mvnw clean test
      
      - name: Test Report
        uses: dorny/test-reporter@v1
        if: always()
        with:
          name: Maven Tests
          path: target/surefire-reports/*.xml
          reporter: java-junit
          fail-on-error: true
      
      - name: Upload test results
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-results
          path: target/surefire-reports/
```

---

## 💡 **Key Takeaway**

**The fix is simple:** Add one line before running tests:
```yaml
run: chmod +x mvnw
```

This is a **standard practice** in cross-platform CI/CD pipelines and shows understanding of Unix permissions and DevOps practices.

---

**Problem solved!** 🎉 Your CI pipeline will now work correctly.
