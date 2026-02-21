# Branch Protection Rules Setup Guide

## 🔒 **The Rule You Want**

**Goal:** "No one can directly push to main, can only merge through PR and all tests must pass"

---

## ⚠️ **IMPORTANT: This is NOT in the .yml file!**

**Common Misconception:**
```
❌ WRONG: Write branch protection in .github/workflows/ci.yml
```

**Correct:**
```
✅ CORRECT: Configure on GitHub website → Settings → Branches
```

---

## 📋 **Step-by-Step Setup**

### **Prerequisites:**
1. Your code is pushed to GitHub
2. `.github/workflows/ci.yml` file exists in the repository
3. You have admin access to the repository

---

### **Steps to Configure:**

#### **1. Go to Repository Settings**
```
1. Open your GitHub repository in browser
2. Click "Settings" tab (top right)
3. Click "Branches" in left sidebar
```

---

#### **2. Add Branch Protection Rule**
```
1. Click "Add rule" button (or "Add branch protection rule")
2. In "Branch name pattern" field, type: main
```

---

#### **3. Configure Protection Settings**

Check these boxes to enforce your rule:

##### **A. Require Pull Request**
```
☑️ Require a pull request before merging
    ☑️ Require approvals: 1 (optional - for code review)
    ☑️ Dismiss stale pull request approvals when new commits are pushed
```

**What this does:**
- ❌ Blocks direct pushes to main
- ✅ Forces pull request workflow
- ✅ Ensures code review (if approvals enabled)

---

##### **B. Require Status Checks**
```
☑️ Require status checks to pass before merging
    ☑️ Require branches to be up to date before merging
    
    Search box: Type "CI" or "test" and select:
    ☑️ CI Pipeline (or whatever your workflow is named)
```

**What this does:**
- ❌ Blocks merging if tests fail
- ✅ Forces tests to pass before merge
- ✅ Ensures branch is up-to-date with main

---

##### **C. Additional Protections (Recommended)**
```
☑️ Require conversation resolution before merging
☑️ Do not allow bypassing the above settings
☑️ Restrict who can push to matching branches (optional)
```

---

#### **4. Save**
```
Click "Create" or "Save changes" at the bottom
```

---

## ✅ **What You've Achieved**

After setup, this is enforced:

```
Developer tries to push directly to main:
❌ Rejected! "Protected branch - cannot push directly"

Developer must:
1. Create feature branch: git checkout -b feature/add-something
2. Push to feature branch: git push origin feature/add-something
3. Create Pull Request on GitHub
4. Wait for CI tests to run
   ↓
   If tests PASS ✅ → Merge button enabled
   If tests FAIL ❌ → Merge button disabled
5. Only then can merge to main
```

---

## 🎯 **Complete Workflow Example**

### **Scenario: Adding a New Feature**

```bash
# 1. Create feature branch (NEVER work on main directly)
git checkout -b feature/add-grades

# 2. Write code and tests
# ... make changes ...

# 3. Commit changes
git add .
git commit -m "Add grade calculation feature"

# 4. Push to feature branch (NOT main)
git push origin feature/add-grades
```

**On GitHub:**
```
5. Go to repository → Click "Pull requests" tab
6. Click "New pull request"
7. Base: main ← compare: feature/add-grades
8. Click "Create pull request"

GitHub Actions automatically:
  ↓
  Runs CI Pipeline (from .yml file)
  ↓
  Tests: 45 tests run...
  ↓
  Result: ✅ All tests passed

Branch Protection Rules check:
  ✅ CI Pipeline: PASSED
  ✅ Branch up to date: YES
  
  Decision: MERGE ALLOWED
  
9. Click "Merge pull request" button (now ENABLED)
10. Click "Confirm merge"
11. Optionally: Delete feature branch
```

---

## 📊 **Two Components Working Together**

### **Component 1: .yml File** (Automation)

**Location:** `.github/workflows/ci.yml`

**Purpose:** RUNS the tests automatically

```yaml
# This RUNS when you push
on:
  push:
  pull_request:

jobs:
  test:
    steps:
      - Run tests
      - Report: ✅ or ❌
```

---

### **Component 2: Branch Protection** (Enforcement)

**Location:** GitHub website → Settings → Branches

**Purpose:** ENFORCES the rules

```
Rules configured on GitHub:
- ❌ Block direct push to main
- ✅ Require pull request
- ✅ Require CI tests to pass
- ✅ Enable merge only if tests pass
```

---

## 🔍 **How to Verify It's Working**

### **Test 1: Try Direct Push (Should Fail)**
```bash
git checkout main
git push origin main
```

**Expected Result:**
```
❌ Error: Protected branch hook declined
❌ Cannot push directly to main
```

---

### **Test 2: Pull Request Workflow (Should Work)**
```bash
# Create branch
git checkout -b test-branch

# Make small change
echo "# Test" >> README.md
git add README.md
git commit -m "Test branch protection"
git push origin test-branch

# Go to GitHub → Create PR
# Watch CI run automatically
# Merge button should be enabled after tests pass ✅
```

---

## 📝 **Visual: Branch Protection UI**

When you configure on GitHub Settings → Branches, you'll see:

```
┌─────────────────────────────────────────────────────┐
│  Branch protection rules                             │
├─────────────────────────────────────────────────────┤
│                                                      │
│  Branch name pattern:  main                         │
│                                                      │
│  ☑️ Require a pull request before merging           │
│     ☑️ Require approvals: 1                         │
│                                                      │
│  ☑️ Require status checks to pass before merging    │
│     ☑️ Require branches to be up to date            │
│                                                      │
│     Status checks that are required:                │
│     🔍 Search: [          ]                         │
│     ☑️ CI Pipeline                                  │
│                                                      │
│  ☑️ Require conversation resolution before merging  │
│                                                      │
│  ☑️ Do not allow bypassing the above settings       │
│                                                      │
│  [Cancel]  [Create] ←────────────────────────       │
│                                                      │
└─────────────────────────────────────────────────────┘
```

---

## 🎓 **For Your Professor**

**How to Explain:**

"I implemented branch protection rules to enforce code quality:

1. **Pull Request Requirement**
   - Direct pushes to main are blocked
   - All changes must go through pull requests
   - Enforces code review workflow

2. **Automated Testing Gate**
   - Every PR triggers GitHub Actions CI pipeline
   - All 45 tests must pass before merge
   - Merge button disabled if tests fail

3. **Configuration**
   - CI automation: `.github/workflows/ci.yml` file
   - Branch protection: GitHub repository settings
   - Together they enforce quality standards

4. **Benefits**
   - Prevents broken code in main branch
   - Enforces team collaboration
   - Automatic quality gates
   - Industry-standard practice"

---

## ⚠️ **Common Mistakes to Avoid**

### **Mistake 1: Writing Rules in .yml**
```yaml
# ❌ WRONG - This doesn't work!
branch-protection:
  require-pr: true
  require-tests: true
```

**Correct:** Configure on GitHub website

---

### **Mistake 2: Forgetting to Select Status Check**
```
☑️ Require status checks to pass before merging

BUT... forgetting to search and select "CI Pipeline"

Result: ❌ Tests run but aren't required!
```

**Solution:** Must explicitly select "CI Pipeline" in the status checks list

---

### **Mistake 3: Not Setting "Do Not Allow Bypassing"**
```
Without this: Admins can bypass rules

With this: ✅ Even admins must follow rules
```

---

## ✅ **Summary Checklist**

Setup complete when:

- [ ] Repository pushed to GitHub
- [ ] `.github/workflows/ci.yml` exists
- [ ] CI workflow has run at least once (so GitHub knows about it)
- [ ] Settings → Branches → Add rule configured
- [ ] "Require a pull request" checked
- [ ] "Require status checks" checked
- [ ] "CI Pipeline" selected in status checks
- [ ] Rule saved
- [ ] Tested: Direct push to main is blocked ❌
- [ ] Tested: PR workflow works ✅

---

## 🚀 **Ready to Use!**

After this setup:
- ✅ No one can push directly to main
- ✅ All changes must go through pull requests
- ✅ All tests must pass before merge
- ✅ Automatic enforcement (no manual checks)

---

**Remember: Branch protection is configured on GitHub website, NOT in code!**
