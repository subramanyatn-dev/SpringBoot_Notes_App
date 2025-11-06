// API Configuration
const API_BASE_URL = window.location.origin;
let token = localStorage.getItem('token');
let userRole = localStorage.getItem('role');
let currentStreamId = null;
let currentSemesterId = null;
let currentSubjectId = null;

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    if (token) {
        showPage('streamsPage');
        loadStreams();
        setupUI();
    } else {
        showPage('loginPage');
    }
    
    setupEventListeners();
});

// Setup Event Listeners
function setupEventListeners() {
    // Login
    document.getElementById('loginForm').addEventListener('submit', handleLogin);
    
    // Register
    document.getElementById('registerForm').addEventListener('submit', handleRegister);
    
    // Logout
    document.getElementById('logoutBtn').addEventListener('click', handleLogout);
    
    // Add buttons
    document.getElementById('addStreamBtn').addEventListener('click', () => openModal('addStreamModal'));
    document.getElementById('addSemesterBtn').addEventListener('click', () => openModal('addSemesterModal'));
    document.getElementById('addSubjectBtn').addEventListener('click', () => openModal('addSubjectModal'));
    document.getElementById('addNoteBtn').addEventListener('click', () => openModal('addNoteModal'));
    
    // Add forms
    document.getElementById('addStreamForm').addEventListener('submit', handleAddStream);
    document.getElementById('addSemesterForm').addEventListener('submit', handleAddSemester);
    document.getElementById('addSubjectForm').addEventListener('submit', handleAddSubject);
    document.getElementById('addNoteForm').addEventListener('submit', handleAddNote);
}

// Show Login Page
function showLoginPage() {
    showPage('loginPage');
    document.getElementById('registerForm').reset();
}

// Show Register Page
function showRegisterPage() {
    showPage('registerPage');
    document.getElementById('loginForm').reset();
}

// Login Handler
async function handleLogin(e) {
    e.preventDefault();
    const email = document.getElementById('loginEmail').value;
    const password = document.getElementById('loginPassword').value;
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password })
        });
        
        if (response.ok) {
            const data = await response.json();
            token = data.accessToken;
            userRole = data.role;
            localStorage.setItem('token', token);
            localStorage.setItem('role', userRole);
            
            showPage('streamsPage');
            loadStreams();
            setupUI();
        } else {
            alert('Login failed. Please check your credentials.');
        }
    } catch (error) {
        console.error('Login error:', error);
        alert('Login failed. Please try again.');
    }
}

// Register Handler
async function handleRegister(e) {
    e.preventDefault();
    const name = document.getElementById('registerName').value;
    const email = document.getElementById('registerEmail').value;
    const password = document.getElementById('registerPassword').value;
    const confirmPassword = document.getElementById('registerConfirmPassword').value;
    const role = 'USER'; // Always register as USER, only admins can be pre-created
    
    if (password !== confirmPassword) {
        alert('Passwords do not match!');
        return;
    }
    
    if (password.length < 4) {
        alert('Password must be at least 4 characters long');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/register`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email, password, role })
        });
        
        if (response.ok) {
            alert('Registration successful! Please login.');
            showLoginPage();
        } else {
            const error = await response.json();
            console.error('Registration failed:', error);
            alert('Registration failed: ' + (error.message || JSON.stringify(error)));
            alert('Registration failed: ' + error);
        }
    } catch (error) {
        console.error('Registration error:', error);
        alert('Registration failed. Please try again.');
    }
}

// Logout Handler
function handleLogout() {
    token = null;
    userRole = null;
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    showPage('loginPage');
    document.getElementById('loginForm').reset();
}

// Setup UI based on role
function setupUI() {
    const isAdmin = userRole === 'ADMIN';
    document.getElementById('addStreamBtn').style.display = isAdmin ? 'block' : 'none';
    document.getElementById('addSemesterBtn').style.display = isAdmin ? 'block' : 'none';
    document.getElementById('addSubjectBtn').style.display = isAdmin ? 'block' : 'none';
    document.getElementById('addNoteBtn').style.display = isAdmin ? 'block' : 'none';
}

// Show Page
function showPage(pageId) {
    document.querySelectorAll('.page').forEach(page => page.classList.remove('active'));
    document.getElementById(pageId).classList.add('active');
}

// Modal Functions
function openModal(modalId) {
    document.getElementById(modalId).classList.add('active');
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('active');
    // Reset forms
    const modal = document.getElementById(modalId);
    const form = modal.querySelector('form');
    if (form) form.reset();
}

// API Call Helper
async function apiCall(url, options = {}) {
    const headers = {
        ...options.headers,
        'Authorization': `Bearer ${token}`
    };
    
    const response = await fetch(`${API_BASE_URL}${url}`, {
        ...options,
        headers
    });
    
    if (response.status === 401) {
        handleLogout();
        throw new Error('Unauthorized');
    }
    
    return response;
}

// Load Streams
async function loadStreams() {
    const list = document.getElementById('streamsList');
    list.innerHTML = '<div class="loading">Loading streams...</div>';
    
    try {
        const response = await apiCall('/streams');
        const streams = await response.json();
        
        if (streams.length === 0) {
            list.innerHTML = `
                <div class="empty-state">
                    <h3>No streams yet</h3>
                    <p>Add your first stream to get started</p>
                </div>
            `;
            return;
        }
        
        list.innerHTML = streams.map(stream => `
            <div class="item" onclick="selectStream(${stream.id})">
                <span class="item-name">${stream.name}</span>
                ${userRole === 'ADMIN' ? `
                    <div class="item-actions" onclick="event.stopPropagation()">
                        <button class="delete-item-btn" onclick="confirmDelete('stream', ${stream.id}, '${stream.name}')">−</button>
                    </div>
                ` : ''}
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading streams:', error);
        list.innerHTML = '<div class="empty-state"><p>Error loading streams</p></div>';
    }
}

// Select Stream
function selectStream(streamId) {
    currentStreamId = streamId;
    showPage('semestersPage');
    loadSemesters(streamId);
}

// Go back to Streams
function goToStreams() {
    currentStreamId = null;
    showPage('streamsPage');
    loadStreams();
}

// Load Semesters
async function loadSemesters(streamId) {
    const list = document.getElementById('semestersList');
    list.innerHTML = '<div class="loading">Loading semesters...</div>';
    
    try {
        const response = await apiCall(`/streams/${streamId}/semesters`);
        const semesters = await response.json();
        
        if (semesters.length === 0) {
            list.innerHTML = `
                <div class="empty-state">
                    <h3>No semesters yet</h3>
                    <p>Add your first semester</p>
                </div>
            `;
            return;
        }
        
        list.innerHTML = semesters.map(semester => `
            <div class="item" onclick="selectSemester(${semester.id})">
                <span class="item-name">Semester ${semester.number}</span>
                ${userRole === 'ADMIN' ? `
                    <div class="item-actions" onclick="event.stopPropagation()">
                        <button class="delete-item-btn" onclick="confirmDelete('semester', ${semester.id}, 'Semester ${semester.number}')">−</button>
                    </div>
                ` : ''}
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading semesters:', error);
        list.innerHTML = '<div class="empty-state"><p>Error loading semesters</p></div>';
    }
}

// Select Semester
function selectSemester(semesterId) {
    currentSemesterId = semesterId;
    showPage('subjectsPage');
    loadSubjects(semesterId);
}

// Go back to Semesters
function goToSemesters() {
    currentSemesterId = null;
    showPage('semestersPage');
    loadSemesters(currentStreamId);
}

// Load Subjects
async function loadSubjects(semesterId) {
    const list = document.getElementById('subjectsList');
    list.innerHTML = '<div class="loading">Loading subjects...</div>';
    
    try {
        const response = await apiCall(`/semesters/${semesterId}/subjects`);
        const subjects = await response.json();
        
        if (subjects.length === 0) {
            list.innerHTML = `
                <div class="empty-state">
                    <h3>No subjects yet</h3>
                    <p>Add your first subject</p>
                </div>
            `;
            return;
        }
        
        list.innerHTML = subjects.map(subject => `
            <div class="item" onclick="selectSubject(${subject.id})">
                <span class="item-name">${subject.name}</span>
                ${userRole === 'ADMIN' ? `
                    <div class="item-actions" onclick="event.stopPropagation()">
                        <button class="delete-item-btn" onclick="confirmDelete('subject', ${subject.id}, '${subject.name}')">−</button>
                    </div>
                ` : ''}
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading subjects:', error);
        list.innerHTML = '<div class="empty-state"><p>Error loading subjects</p></div>';
    }
}

// Select Subject
function selectSubject(subjectId) {
    currentSubjectId = subjectId;
    showPage('notesPage');
    loadNotes(subjectId);
}

// Go back to Subjects
function goToSubjects() {
    currentSubjectId = null;
    showPage('subjectsPage');
    loadSubjects(currentSemesterId);
}

// Load Notes
async function loadNotes(subjectId) {
    const list = document.getElementById('notesList');
    list.innerHTML = '<div class="loading">Loading notes...</div>';
    
    try {
        const response = await apiCall(`/subjects/${subjectId}/notes`);
        const notes = await response.json();
        
        if (notes.length === 0) {
            list.innerHTML = `
                <div class="empty-state">
                    <h3>No notes yet</h3>
                    <p>Upload your first note</p>
                </div>
            `;
            return;
        }
        
        list.innerHTML = notes.map(note => `
            <div class="item">
                <a href="${note.fileUrl}" target="_blank" class="note-link">
                    <span class="note-icon">📄</span>
                    <span class="item-name">${note.title}</span>
                </a>
                ${userRole === 'ADMIN' ? `
                    <div class="item-actions">
                        <button class="delete-item-btn" onclick="confirmDelete('note', '${note.id}', '${note.title}')">−</button>
                    </div>
                ` : ''}
            </div>
        `).join('');
    } catch (error) {
        console.error('Error loading notes:', error);
        list.innerHTML = '<div class="empty-state"><p>Error loading notes</p></div>';
    }
}

// Add Stream
async function handleAddStream(e) {
    e.preventDefault();
    const name = document.getElementById('streamName').value;
    
    try {
        const response = await apiCall('/streams', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name })
        });
        
        if (response.ok) {
            closeModal('addStreamModal');
            loadStreams();
        } else {
            alert('Failed to add stream');
        }
    } catch (error) {
        console.error('Error adding stream:', error);
        alert('Failed to add stream');
    }
}

// Add Semester
async function handleAddSemester(e) {
    e.preventDefault();
    const number = parseInt(document.getElementById('semesterNumber').value);
    
    try {
        const response = await apiCall(`/streams/${currentStreamId}/semesters`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ number })
        });
        
        if (response.ok) {
            closeModal('addSemesterModal');
            loadSemesters(currentStreamId);
        } else {
            alert('Failed to add semester');
        }
    } catch (error) {
        console.error('Error adding semester:', error);
        alert('Failed to add semester');
    }
}

// Add Subject
async function handleAddSubject(e) {
    e.preventDefault();
    const name = document.getElementById('subjectName').value;
    
    try {
        const response = await apiCall(`/semesters/${currentSemesterId}/subjects`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name })
        });
        
        if (response.ok) {
            closeModal('addSubjectModal');
            loadSubjects(currentSemesterId);
        } else {
            alert('Failed to add subject');
        }
    } catch (error) {
        console.error('Error adding subject:', error);
        alert('Failed to add subject');
    }
}

// Add Note
async function handleAddNote(e) {
    e.preventDefault();
    const title = document.getElementById('noteTitle').value;
    const file = document.getElementById('noteFile').files[0];
    
    if (!file) {
        alert('Please select a file');
        return;
    }
    
    const formData = new FormData();
    formData.append('title', title);
    formData.append('file', file);
    
    try {
        const response = await apiCall(`/subjects/${currentSubjectId}/notes`, {
            method: 'POST',
            body: formData
        });
        
        if (response.ok) {
            closeModal('addNoteModal');
            loadNotes(currentSubjectId);
        } else {
            alert('Failed to upload note');
        }
    } catch (error) {
        console.error('Error uploading note:', error);
        alert('Failed to upload note');
    }
}

// Confirm Delete
let deleteType = null;
let deleteId = null;

function confirmDelete(type, id, name) {
    deleteType = type;
    deleteId = id;
    document.getElementById('deleteMessage').textContent = `Are you sure you want to delete "${name}"?`;
    openModal('deleteModal');
}

document.getElementById('confirmDeleteBtn').addEventListener('click', async () => {
    if (!deleteType || !deleteId) return;
    
    const endpoints = {
        stream: `/streams/${deleteId}`,
        semester: `/semesters/${deleteId}`,
        subject: `/subjects/${deleteId}`,
        note: `/notes/${deleteId}`
    };
    
    try {
        const response = await apiCall(endpoints[deleteType], {
            method: 'DELETE'
        });
        
        if (response.ok) {
            closeModal('deleteModal');
            
            // Reload appropriate list
            switch (deleteType) {
                case 'stream':
                    loadStreams();
                    break;
                case 'semester':
                    loadSemesters(currentStreamId);
                    break;
                case 'subject':
                    loadSubjects(currentSemesterId);
                    break;
                case 'note':
                    loadNotes(currentSubjectId);
                    break;
            }
        } else {
            alert('Failed to delete');
        }
    } catch (error) {
        console.error('Error deleting:', error);
        alert('Failed to delete');
    }
});
