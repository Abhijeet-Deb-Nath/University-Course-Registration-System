// ========== State ==========
const state = {
  token: localStorage.getItem("accessToken") || "",
  role: localStorage.getItem("userRole") || "",
  username: localStorage.getItem("username") || ""
};

// ========== DOM Helpers ==========
const $ = (id) => document.getElementById(id);
const page = document.body.getAttribute("data-page");

// ========== Token Helpers ==========
function decodeToken(token) {
  const parts = token.split(".");
  if (parts.length !== 3) return null;
  try {
    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    return JSON.parse(atob(base64));
  } catch (e) {
    return null;
  }
}

function isTokenExpired(token) {
  const payload = decodeToken(token);
  if (!payload || !payload.exp) return true;
  const now = Math.floor(Date.now() / 1000);
  return payload.exp < now;
}

function saveSession(token) {
  const payload = decodeToken(token);
  if (!payload) return false;
  state.token = token;
  state.role = payload.role || "";
  state.username = payload.sub || "";
  localStorage.setItem("accessToken", token);
  localStorage.setItem("userRole", state.role);
  localStorage.setItem("username", state.username);
  return true;
}

function clearSession() {
  state.token = "";
  state.role = "";
  state.username = "";
  localStorage.removeItem("accessToken");
  localStorage.removeItem("userRole");
  localStorage.removeItem("username");
}

function redirectToDashboard() {
  if (state.role === "TEACHER") {
    window.location.href = "/teacher.html";
  } else if (state.role === "STUDENT") {
    window.location.href = "/student.html";
  } else {
    window.location.href = "/index.html";
  }
}

function requireAuth(allowedRole) {
  if (!state.token || !state.role || isTokenExpired(state.token)) {
    clearSession();
    window.location.href = "/index.html";
    return false;
  }
  if (allowedRole && state.role !== allowedRole) {
    redirectToDashboard();
    return false;
  }
  return true;
}

// ========== API Helper ==========
async function apiRequest(method, url, body, useAuth = true) {
  const headers = { "Content-Type": "application/json" };
  if (useAuth && state.token) {
    headers.Authorization = "Bearer " + state.token;
  }

  const response = await fetch(url, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined
  });

  // Handle 401 Unauthorized - token expired or invalid
  if (response.status === 401 && useAuth) {
    clearSession();
    window.location.href = "/index.html";
    return { status: 401, data: { message: "Session expired. Please login again." } };
  }

  let data = null;
  try {
    data = await response.json();
  } catch (e) {
    data = await response.text();
  }

  setResult(response.status, data);
  return { status: response.status, data };
}

function setResult(status, data) {
  const statusLine = $("status-line");
  const responseOutput = $("response-output");
  if (!statusLine || !responseOutput) return;
  statusLine.textContent = "Status: " + status;
  responseOutput.textContent = typeof data === "object" ? JSON.stringify(data, null, 2) : String(data || "");
}

function showError(elementId, message) {
  const el = $(elementId);
  if (el) el.textContent = message;
}

function readValue(id) {
  const el = $(id);
  return el ? el.value : "";
}

function readNumber(id) {
  const el = $(id);
  return el && el.value ? Number(el.value) : null;
}

// ========== User Display ==========
function showUserInfo() {
  const display = $("user-display");
  if (display) {
    display.textContent = state.username + " (" + state.role + ")";
  }
}

// ========== Logout ==========
function bindLogout() {
  const btn = $("logout-btn");
  if (btn) {
    btn.addEventListener("click", () => {
      clearSession();
      window.location.href = "/index.html";
    });
  }
}

// ========== Home Page (Login) ==========
function initHomePage() {
  // If already logged in with valid token, redirect to dashboard
  if (state.token && state.role && !isTokenExpired(state.token)) {
    redirectToDashboard();
    return;
  }
  // Clear any expired session
  if (state.token && isTokenExpired(state.token)) {
    clearSession();
  }

  const loginBtn = $("login-btn");
  if (loginBtn) {
    loginBtn.addEventListener("click", async () => {
      showError("login-error", "");
      const result = await apiRequest("POST", "/api/auth/login", {
        username: readValue("login-username"),
        password: readValue("login-password")
      }, false);

      if (result.status === 200 && result.data && result.data.accessToken) {
        if (saveSession(result.data.accessToken)) {
          redirectToDashboard();
        }
      } else {
        const msg = result.data && result.data.message ? result.data.message : "Login failed";
        showError("login-error", msg);
      }
    });
  }
}

// ========== Register Page ==========
function initRegisterPage() {
  const registerBtn = $("register-btn");
  if (registerBtn) {
    registerBtn.addEventListener("click", async () => {
      showError("register-error", "");
      const result = await apiRequest("POST", "/api/auth/register", {
        username: readValue("reg-username"),
        password: readValue("reg-password"),
        role: readValue("reg-role")
      }, false);

      if (result.status === 201) {
        window.location.href = "/index.html";
      } else {
        const msg = result.data && result.data.message ? result.data.message : "Registration failed";
        showError("register-error", msg);
      }
    });
  }
}

// ========== Student Page ==========
function initStudentPage() {
  if (!requireAuth("STUDENT")) return;
  showUserInfo();
  bindLogout();

  const allBtn = $("courses-all-btn");
  const registerBtn = $("register-course-btn");
  const dropBtn = $("drop-course-btn");
  const mineBtn = $("registrations-mine-btn");

  if (allBtn) {
    allBtn.addEventListener("click", async () => {
      await apiRequest("GET", "/api/courses");
    });
  }

  if (registerBtn) {
    registerBtn.addEventListener("click", async () => {
      const courseId = readNumber("register-course-id");
      if (!courseId) {
        setResult("-", "Course Id is required");
        return;
      }
      await apiRequest("POST", "/api/registrations", { courseId });
    });
  }

  if (dropBtn) {
    dropBtn.addEventListener("click", async () => {
      const courseId = readNumber("drop-course-id");
      if (!courseId) {
        setResult("-", "Course Id is required");
        return;
      }
      await apiRequest("DELETE", "/api/registrations", { courseId });
    });
  }

  if (mineBtn) {
    mineBtn.addEventListener("click", async () => {
      await apiRequest("GET", "/api/registrations/mine");
    });
  }
}

// ========== Teacher Page ==========
function initTeacherPage() {
  if (!requireAuth("TEACHER")) return;
  showUserInfo();
  bindLogout();

  const createBtn = $("course-create-btn");
  const updateBtn = $("course-update-btn");
  const deleteBtn = $("course-delete-btn");
  const mineBtn = $("courses-mine-btn");
  const studentsBtn = $("course-students-btn");

  if (createBtn) {
    createBtn.addEventListener("click", async () => {
      await apiRequest("POST", "/api/courses", {
        courseNo: readValue("course-no"),
        courseName: readValue("course-name")
      });
    });
  }

  if (updateBtn) {
    updateBtn.addEventListener("click", async () => {
      const courseId = readNumber("course-update-id");
      if (!courseId) {
        setResult("-", "Course Id is required");
        return;
      }
      await apiRequest("PUT", "/api/courses/" + courseId, {
        courseNo: readValue("course-update-no"),
        courseName: readValue("course-update-name")
      });
    });
  }

  if (deleteBtn) {
    deleteBtn.addEventListener("click", async () => {
      const courseId = readNumber("course-delete-id");
      if (!courseId) {
        setResult("-", "Course Id is required");
        return;
      }
      await apiRequest("DELETE", "/api/courses/" + courseId);
    });
  }

  if (mineBtn) {
    mineBtn.addEventListener("click", async () => {
      await apiRequest("GET", "/api/courses/mine");
    });
  }

  if (studentsBtn) {
    studentsBtn.addEventListener("click", async () => {
      const courseId = readNumber("course-students-id");
      if (!courseId) {
        setResult("-", "Course Id is required");
        return;
      }
      await apiRequest("GET", "/api/courses/" + courseId + "/students");
    });
  }
}

// ========== Init ==========
if (page === "home") {
  initHomePage();
} else if (page === "register") {
  initRegisterPage();
} else if (page === "student") {
  initStudentPage();
} else if (page === "teacher") {
  initTeacherPage();
}
