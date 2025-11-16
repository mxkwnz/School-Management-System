const API_BASE = 'http://localhost:8080';

let currentUser = null;
let allSubjects = [];

// Initialize dashboard
document.addEventListener('DOMContentLoaded', function() {
    // Check if user is logged in
    const userStr = localStorage.getItem('user');
    if (!userStr) {
        window.location.href = 'login.html';
        return;
    }

    currentUser = JSON.parse(userStr);
    displayUserInfo();
    loadDashboard();
    loadStudentsForDropdowns();
    loadSubjectsForDropdowns();
    loadUsersCache();
    
    // Setup subject listener for loading current grade
    const subjectSelect = document.getElementById('gradeSubjectSelect');
    if (subjectSelect) {
        subjectSelect.addEventListener('change', function() {
            const studentSelect = document.getElementById('gradeStudentSelect');
            if (studentSelect && studentSelect.value && this.value) {
                loadCurrentGrade(studentSelect.value, this.value);
            }
        });
    }
});

let allStudents = [];
let allUsers = [];

async function loadStudentsForDropdowns() {
    try {
        const response = await fetch(`${API_BASE}/api/students/all`);
        const result = await response.json();
        
        if (result.success) {
            allStudents = result.students;
            populateStudentDropdowns();
        }
    } catch (error) {
        console.error('Error loading students:', error);
    }
}

function populateStudentDropdowns() {
    const gradeSelect = document.getElementById('gradeStudentSelect');
    const attSelect = document.getElementById('attStudentSelect');
    const msgSelect = document.getElementById('messageStudentSelect');
    
    if (gradeSelect) {
        gradeSelect.innerHTML = '<option value="">-- Select a student --</option>';
        allStudents.forEach(student => {
            const option = document.createElement('option');
            option.value = student.userId;
            option.textContent = `${student.firstName} ${student.lastName} (${student.userId})`;
            option.dataset.firstName = student.firstName;
            option.dataset.lastName = student.lastName;
            option.dataset.pk = student.id;
            gradeSelect.appendChild(option);
        });
    }
    
    if (attSelect) {
        attSelect.innerHTML = '<option value="">-- Select a student --</option>';
        allStudents.forEach(student => {
            const option = document.createElement('option');
            option.value = student.userId;
            option.textContent = `${student.firstName} ${student.lastName} (${student.userId})`;
            option.dataset.firstName = student.firstName;
            option.dataset.lastName = student.lastName;
            option.dataset.pk = student.id;
            attSelect.appendChild(option);
        });
    }

    if (msgSelect) {
        msgSelect.innerHTML = '<option value="">-- Select a student --</option>';
        allStudents.forEach(student => {
            const option = document.createElement('option');
            option.value = student.userId;
            option.textContent = `${student.firstName} ${student.lastName} (${student.userId})`;
            option.dataset.firstName = student.firstName;
            option.dataset.lastName = student.lastName;
            option.dataset.pk = student.id;
            msgSelect.appendChild(option);
        });
    }
}

async function loadSubjectsForDropdowns() {
    try {
        const response = await fetch(`${API_BASE}/api/subjects/all`);
        const result = await response.json();
        if (result.success) {
            allSubjects = result.subjects || [];
            populateSubjectDropdowns();
        }
    } catch (error) {
        console.error('Error loading subjects:', error);
    }
}

// Load all users (for things like advisor selection)
async function loadUsersCache() {
    try {
        const response = await fetch(`${API_BASE}/api/users/all`);
        const result = await response.json();
        if (result.success) {
            allUsers = result.users;
            // After users are loaded, populate advisor dropdown for students
            populateAdvisorDropdown();
        }
    } catch (error) {
        console.error('Error loading users:', error);
    }
}

function populateSubjectDropdowns() {
    const gradeSubjectSelect = document.getElementById('gradeSubjectSelect');
    const attSubjectSelect = document.getElementById('attSubjectSelect');

    if (gradeSubjectSelect) {
        gradeSubjectSelect.innerHTML = '<option value="">-- Select a subject --</option>';
        allSubjects.forEach(subject => {
            const option = document.createElement('option');
            option.value = subject.code;
            option.textContent = `${subject.name} (${subject.code})`;
            option.dataset.id = subject.id;
            gradeSubjectSelect.appendChild(option);
        });
    }

    if (attSubjectSelect) {
        attSubjectSelect.innerHTML = '<option value="">-- Select a subject --</option>';
        allSubjects.forEach(subject => {
            const option = document.createElement('option');
            option.value = subject.code;
            option.textContent = `${subject.name} (${subject.code})`;
            option.dataset.id = subject.id;
            attSubjectSelect.appendChild(option);
        });
    }
}

function onStudentSelectChange(selectElement, type) {
    const selectedOption = selectElement.options[selectElement.selectedIndex];
    if (selectedOption && selectedOption.value) {
        const firstName = selectedOption.dataset.firstName;
        const lastName = selectedOption.dataset.lastName;
        if (type === 'attendance') {
            // Student name is auto-filled from dropdown
        } else if (type === 'grade') {
            // Load current grade when student and subject are selected
            const subjectSelect = document.getElementById('gradeSubjectSelect');
            const subject = subjectSelect ? subjectSelect.value : '';
            if (subject) {
                loadCurrentGrade(selectedOption.value, subject);
            }
        }
    }
}

function displayUserInfo() {
    // Display firstName and lastName separately
    const firstName = currentUser.firstName || '';
    const lastName = currentUser.lastName || '';
    
    document.getElementById('userFirstName').textContent = firstName;
    document.getElementById('userLastName').textContent = lastName;
    
    const roleBadge = document.getElementById('userRole');
    if (currentUser.roles && currentUser.roles.length > 0) {
        roleBadge.textContent = currentUser.roles[0];
        roleBadge.className = 'badge present';
    }
}

function loadDashboard() {
    const roles = currentUser.roles || [];
    
    if (roles.includes('STUDENT')) {
        showStudentDashboard();
        loadStudentData();
        showNotificationsSection();
    } else if (roles.includes('PARENT')) {
        showParentDashboard();
        loadParentData();
        showNotificationsSection();
    } else if (roles.includes('ADVISOR') && !roles.includes('TEACHER') && !roles.includes('ADMIN')) {
        // Pure advisor: limited dashboard focused on messaging only
        showAdvisorDashboard();
        showNotificationsSection();
    } else if (roles.includes('TEACHER')) {
        showTeacherDashboard();
        showNotificationsSection();
    } else if (roles.includes('ADMIN')) {
        showAdminDashboard();
        loadAllUsers();
        loadStaffList();
        hideNotificationsSection();
    } else {
        // Default to student view
        showStudentDashboard();
        loadStudentData();
        showNotificationsSection();
    }
    
    // Load notifications for non-admin users
    if (!roles.includes('ADMIN')) {
        loadNotifications();
    }
}

function showNotificationsSection() {
    const section = document.getElementById('notificationsSection');
    if (section) {
        section.style.display = 'block';
    }
}

function hideNotificationsSection() {
    const section = document.getElementById('notificationsSection');
    if (section) {
        section.style.display = 'none';
    }
}

// Advisor dashboard reuses teacher layout but hides management / grades / attendance
function showAdvisorDashboard() {
    // Activate teacher layout
    document.getElementById('studentDashboard').classList.remove('active');
    document.getElementById('teacherDashboard').classList.add('active');
    document.getElementById('adminDashboard').classList.remove('active');

    // Rename header to Advisor Dashboard
    const header = document.querySelector('#teacherDashboard h2');
    if (header) {
        header.textContent = 'Advisor Dashboard';
    }

    // Hide tabs that are not allowed for advisors
    const manageBtn = document.querySelector('#teacherDashboard .tabs button[onclick*="manage"]');
    const gradesBtn = document.querySelector('#teacherDashboard .tabs button[onclick*="grades"]');
    const attendanceBtn = document.querySelector('#teacherDashboard .tabs button[onclick*="attendance"]');
    if (manageBtn) manageBtn.style.display = 'none';
    if (gradesBtn) gradesBtn.style.display = 'none';
    if (attendanceBtn) attendanceBtn.style.display = 'none';

    // Hide corresponding content sections
    const manageContent = document.getElementById('teacherManage');
    const gradesContent = document.getElementById('teacherGrades');
    const attendanceContent = document.getElementById('teacherAttendance');
    if (manageContent) manageContent.style.display = 'none';
    if (gradesContent) gradesContent.style.display = 'none';
    if (attendanceContent) attendanceContent.style.display = 'none';

    // Ensure only messages tab is active
    document.querySelectorAll('#teacherDashboard .tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    const messagesContent = document.getElementById('teacherMessages');
    if (messagesContent) {
        messagesContent.classList.add('active');
    }

    document.querySelectorAll('#teacherDashboard .tab-button').forEach(btn => {
        btn.classList.remove('active');
    });
    const messagesBtn = document.querySelector('#teacherDashboard .tabs button[onclick*="messages"]');
    if (messagesBtn) {
        messagesBtn.classList.add('active');
    }
}

function showStudentDashboard() {
    document.getElementById('studentDashboard').classList.add('active');
    document.getElementById('teacherDashboard').classList.remove('active');
    document.getElementById('adminDashboard').classList.remove('active');
}

// ---- Student ↔ Advisor messaging ----

function populateAdvisorDropdown() {
    const select = document.getElementById('studentAdvisorSelect');
    if (!select || !allUsers || allUsers.length === 0) return;

    select.innerHTML = '<option value="">-- Select an advisor --</option>';
    allUsers
        .filter(u => Array.isArray(u.roles) && u.roles.includes('ADVISOR'))
        .forEach(advisor => {
            const option = document.createElement('option');
            option.value = advisor.userId;
            option.textContent = `${advisor.firstName || ''} ${advisor.lastName || ''} (${advisor.userId})`;
            option.dataset.pk = advisor.id;
            select.appendChild(option);
        });
}

async function onStudentAdvisorChange() {
    const select = document.getElementById('studentAdvisorSelect');
    const option = select ? select.options[select.selectedIndex] : null;
    const container = document.getElementById('studentMessagesContainer');
    if (!option || !option.dataset.pk) {
        if (container) {
            container.innerHTML = '<p style="color: #9ca3af; text-align: center;">Select an advisor to view the conversation.</p>';
        }
        return;
    }
    const advisorPk = option.dataset.pk;
    await loadConversationWithAdvisor(advisorPk);
}

async function loadConversationWithAdvisor(advisorPk) {
    if (!currentUser || typeof currentUser.id === 'undefined' || currentUser.id === null) {
        console.error('Current user id is missing.');
        return;
    }
    const partnerPk = Number(advisorPk);
    if (!Number.isFinite(partnerPk)) {
        console.error('Advisor PK is invalid or undefined:', advisorPk);
        return;
    }
    try {
        const response = await fetch(
            `${API_BASE}/api/messages/conversation?userPkA=${currentUser.id}&userPkB=${partnerPk}`
        );
        const result = await response.json();
        if (result.success) {
            renderMessages(result.messages, partnerPk, 'studentMessagesContainer');
        }
    } catch (error) {
        console.error('Error loading advisor conversation:', error);
    }
}

async function sendMessageToAdvisor(event) {
    event.preventDefault();
    const select = document.getElementById('studentAdvisorSelect');
    const option = select ? select.options[select.selectedIndex] : null;
    if (!option || !option.dataset.pk) {
        alert('Please select an advisor first.');
        return;
    }
    const advisorPk = Number(option.dataset.pk);
    if (!Number.isFinite(advisorPk)) {
        alert('Selected advisor has invalid id.');
        return;
    }
    const contentInput = document.getElementById('studentMessageContent');
    const content = contentInput ? contentInput.value.trim() : '';
    if (!content) return;

    try {
        const response = await fetch(`${API_BASE}/api/messages/send`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                senderPk: currentUser.id,
                receiverPk: advisorPk,
                content: content
            })
        });
        const result = await response.json();
        if (result.success) {
            contentInput.value = '';
            await loadConversationWithAdvisor(advisorPk);
        } else {
            alert('Error sending message: ' + (result.error || 'Unknown error'));
        }
    } catch (error) {
        alert('Error sending message: ' + error.message);
    }
}

function showTeacherDashboard() {
    document.getElementById('studentDashboard').classList.remove('active');
    document.getElementById('teacherDashboard').classList.add('active');
    document.getElementById('adminDashboard').classList.remove('active');
    const dateInput = document.getElementById('attDate');
    if (dateInput) {
        dateInput.valueAsDate = new Date();
    }

    // For pure teachers (not admin/advisor), restrict to Grades & Attendance only
    const roles = currentUser.roles || [];
    const isPureTeacher = roles.includes('TEACHER') && !roles.includes('ADMIN') && !roles.includes('ADVISOR');

    const manageBtn = document.querySelector('#teacherDashboard .tabs button[onclick*="manage"]');
    const messagesBtn = document.querySelector('#teacherDashboard .tabs button[onclick*="messages"]');
    const gradesBtn = document.querySelector('#teacherDashboard .tabs button[onclick*="grades"]');
    const attendanceBtn = document.querySelector('#teacherDashboard .tabs button[onclick*="attendance"]');

    const manageContent = document.getElementById('teacherManage');
    const messagesContent = document.getElementById('teacherMessages');

    if (isPureTeacher) {
        // Hide manage and messages for teachers
        if (manageBtn) manageBtn.style.display = 'none';
        if (messagesBtn) messagesBtn.style.display = 'none';
        if (manageContent) manageContent.style.display = 'none';
        if (messagesContent) messagesContent.style.display = 'none';

        // Ensure Grades tab is active by default
        document.querySelectorAll('#teacherDashboard .tab-content').forEach(tab => {
            tab.classList.remove('active');
        });
        if (document.getElementById('teacherGrades')) {
            document.getElementById('teacherGrades').classList.add('active');
        }
        document.querySelectorAll('#teacherDashboard .tab-button').forEach(btn => {
            btn.classList.remove('active');
        });
        if (gradesBtn) gradesBtn.classList.add('active');
    } else {
        // For admins (and others), show all teacher tabs
        if (manageBtn) manageBtn.style.display = '';
        if (messagesBtn) messagesBtn.style.display = '';
        if (manageContent) manageContent.style.display = '';
        if (messagesContent) messagesContent.style.display = '';
    }

    // Load a teaching timetable view for teachers/admins
    loadTeacherTimetable();
}

async function loadTeacherTimetable() {
    try {
        const response = await fetch(`${API_BASE}/api/timetable/parttime`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                year: '2024-2025',
                trimester: 'second'
            })
        });
        const result = await response.json();
        if (result.success) {
            renderTimetable(result.entries || [], 'teacherTimetableBody', false);
        }
    } catch (error) {
        console.error('Error loading teacher timetable:', error);
    }
}

function showParentDashboard() {
    document.getElementById('studentDashboard').classList.remove('active');
    document.getElementById('teacherDashboard').classList.remove('active');
    document.getElementById('adminDashboard').classList.remove('active');
    document.getElementById('parentDashboard').classList.add('active');
}

function showAdminDashboard() {
    document.getElementById('studentDashboard').classList.remove('active');
    document.getElementById('parentDashboard').classList.remove('active');
    document.getElementById('adminDashboard').classList.add('active');

    // Also enable teacher Grades & Attendance tools for admin
    document.getElementById('teacherDashboard').classList.add('active');

    const header = document.querySelector('#teacherDashboard h2');
    if (header) {
        header.textContent = 'Teacher / Admin Tools';
    }

    const manageBtn = document.querySelector('#teacherDashboard .tabs button[onclick*="manage"]');
    const messagesBtn = document.querySelector('#teacherDashboard .tabs button[onclick*="messages"]');
    const manageContent = document.getElementById('teacherManage');
    const messagesContent = document.getElementById('teacherMessages');

    // Admin should not see "Manage Students" here (handled via admin tabs)
    if (manageBtn) manageBtn.style.display = 'none';
    if (manageContent) manageContent.style.display = 'none';

    // Admin can still use messages if desired
    if (messagesBtn) messagesBtn.style.display = '';
    if (messagesContent) messagesContent.style.display = '';

    // Load overview timetable for admin
    loadAdminTimetable();
}

async function loadAdminTimetable() {
    try {
        const response = await fetch(`${API_BASE}/api/timetable/student`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                studentName: 'Overview',
                year: '2025-2026',
                trimester: 'first'
            })
        });
        const result = await response.json();
        if (result.success) {
            renderTimetable(result.entries || [], 'adminTimetableBody', true);
        }
    } catch (error) {
        console.error('Error loading admin timetable:', error);
    }
}

function showTeacherTab(tabName) {
    document.querySelectorAll('#teacherDashboard .tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelectorAll('#teacherDashboard .tab-button').forEach(btn => {
        btn.classList.remove('active');
    });
    
    document.getElementById('teacher' + tabName.charAt(0).toUpperCase() + tabName.slice(1)).classList.add('active');
    event.target.classList.add('active');
}

function onMessageStudentChange() {
    const select = document.getElementById('messageStudentSelect');
    const option = select.options[select.selectedIndex];
    if (!option || !option.dataset.pk) {
        document.getElementById('messagesContainer').innerHTML =
            '<p style="color: #9ca3af; text-align: center;">Select a student to view the conversation.</p>';
        return;
    }
    const studentPk = option.dataset.pk;
    loadConversationWithStudent(studentPk);
}

async function loadConversationWithStudent(studentPk) {
    // Ensure we have a valid current user and partner primary key
    if (!currentUser || typeof currentUser.id === 'undefined' || currentUser.id === null) {
        console.error('Current user id is missing.');
        return;
    }
    const partnerPk = Number(studentPk);
    if (!Number.isFinite(partnerPk)) {
        console.error('Student PK is invalid or undefined:', studentPk);
        return;
    }
    try {
        const response = await fetch(
            `${API_BASE}/api/messages/conversation?userPkA=${currentUser.id}&userPkB=${partnerPk}`
        );
        const result = await response.json();
        if (result.success) {
            renderMessages(result.messages, partnerPk, 'messagesContainer');
        }
    } catch (error) {
        console.error('Error loading conversation:', error);
    }
}

function renderMessages(messages, partnerPk, containerId = 'messagesContainer') {
    const container = document.getElementById(containerId);
    container.innerHTML = '';
    if (!messages || messages.length === 0) {
        container.innerHTML = '<p style="color: #9ca3af; text-align: center;">No messages yet. Start the conversation!</p>';
        return;
    }
    messages.forEach(msg => {
        const isMine = msg.senderPk === currentUser.id;
        const msgDiv = document.createElement('div');
        msgDiv.style.marginBottom = '8px';
        msgDiv.style.display = 'flex';
        msgDiv.style.justifyContent = isMine ? 'flex-end' : 'flex-start';
        const bubble = document.createElement('div');
        bubble.style.maxWidth = '70%';
        bubble.style.padding = '8px 12px';
        bubble.style.borderRadius = '12px';
        bubble.style.fontSize = '0.95em';
        bubble.style.whiteSpace = 'pre-wrap';
        bubble.style.boxShadow = '0 1px 3px rgba(15,23,42,0.1)';
        bubble.style.background = isMine ? '#4f46e5' : '#e5e7eb';
        bubble.style.color = isMine ? '#ffffff' : '#111827';
        bubble.textContent = msg.content;
        msgDiv.appendChild(bubble);
        container.appendChild(msgDiv);
    });
    container.scrollTop = container.scrollHeight;
}

async function sendMessageToStudent(event) {
    event.preventDefault();
    const select = document.getElementById('messageStudentSelect');
    const option = select.options[select.selectedIndex];
    if (!option || !option.dataset.pk) {
        alert('Please select a student first.');
        return;
    }
    const studentPk = Number(option.dataset.pk);
    if (!Number.isFinite(studentPk)) {
        alert('Selected student has invalid id.');
        return;
    }
    const contentInput = document.getElementById('messageContent');
    const content = contentInput.value.trim();
    if (!content) return;

    try {
        const response = await fetch(`${API_BASE}/api/messages/send`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                senderPk: currentUser.id,
                receiverPk: studentPk,
                content: content
            })
        });
        const result = await response.json();
        if (result.success) {
            contentInput.value = '';
            loadConversationWithStudent(studentPk);
        } else {
            alert('Error sending message: ' + (result.error || 'Unknown error'));
        }
    } catch (error) {
        alert('Error sending message: ' + error.message);
    }
}

function showAdminTab(tabName) {
    document.querySelectorAll('#adminDashboard .tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelectorAll('#adminDashboard .tab-button').forEach(btn => {
        btn.classList.remove('active');
    });
    
    const tabId = 'admin' + tabName.charAt(0).toUpperCase() + tabName.slice(1);
    const tabElement = document.getElementById(tabId);
    if (tabElement) {
        tabElement.classList.add('active');
    }
    if (event && event.target) {
        event.target.classList.add('active');
    }
    
    if (tabName === 'users') {
        loadAllUsers();
    } else if (tabName === 'staff') {
        loadStaffList();
    } else if (tabName === 'staffOnboard') {
        // Staff onboarding form is already visible
    } else if (tabName === 'parent') {
        loadParentLinkForm();
    }
}

async function loadStudentData() {
    if (!currentUser || !currentUser.userId) return;

    try {
        // Load grades
        const gradesResponse = await fetch(`${API_BASE}/api/user/grades?userId=${currentUser.userId}`);
        const gradesData = await gradesResponse.json();
        
        if (gradesData.success) {
            displayGrades(gradesData.grades);
            calculateAverageGrade(gradesData.grades);
        }

        // Load attendance
        const attendanceResponse = await fetch(`${API_BASE}/api/user/attendance?userId=${currentUser.userId}`);
        const attendanceData = await attendanceResponse.json();
        
        if (attendanceData.success) {
            displayAttendance(attendanceData.attendance);
            document.getElementById('studentAttendance').textContent = attendanceData.percentage.toFixed(1);
        }

        // Load timetable (default year/trimester for now)
        await loadStudentTimetable(currentUser.name || currentUser.userId);
    } catch (error) {
        console.error('Error loading student data:', error);
    }
}

async function loadStudentTimetable(studentName) {
    try {
        const response = await fetch(`${API_BASE}/api/timetable/student`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                studentName: studentName || 'Student',
                year: '2024-2025',
                trimester: 'first'
            })
        });
        const result = await response.json();
        if (result.success) {
            renderTimetable(result.entries || [], 'studentTimetableBody', true);
        }
    } catch (error) {
        console.error('Error loading student timetable:', error);
    }
}

function displayGrades(grades) {
    const tbody = document.getElementById('gradesBody');
    tbody.innerHTML = '';

    if (grades.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align: center;">No grades available</td></tr>';
        return;
    }

    grades.forEach(grade => {
        const row = document.createElement('tr');
        const letterGrade = getLetterGrade(grade.score);
        const status = grade.score >= 60 ? 'PASS' : 'FAIL';
        row.innerHTML = `
            <td>${grade.subject}</td>
            <td>${grade.score}</td>
            <td>${letterGrade}</td>
            <td><span class="badge ${status === 'PASS' ? 'present' : 'absent'}">${status}</span></td>
            <td>${new Date(grade.updatedAt).toLocaleDateString()}</td>
        `;
        tbody.appendChild(row);
    });

    document.getElementById('studentSubjects').textContent = grades.length;
}

function displayAttendance(attendance) {
    const tbody = document.getElementById('attendanceBody');
    tbody.innerHTML = '';

    if (attendance.length === 0) {
        tbody.innerHTML = '<tr><td colspan="3" style="text-align: center;">No attendance records</td></tr>';
        return;
    }

    attendance.forEach(att => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${new Date(att.date).toLocaleDateString()}</td>
            <td>${att.subject || 'N/A'}</td>
            <td><span class="badge ${att.present ? 'present' : 'absent'}">${att.present ? 'Present' : 'Absent'}</span></td>
        `;
        tbody.appendChild(row);
    });
}

function calculateAverageGrade(grades) {
    if (grades.length === 0) {
        document.getElementById('studentAvgGrade').textContent = '-';
        return;
    }

    const sum = grades.reduce((acc, grade) => acc + grade.score, 0);
    const avg = sum / grades.length;
    document.getElementById('studentAvgGrade').textContent = avg.toFixed(1);
}

function getLetterGrade(score) {
    if (score >= 90) return 'A';
    if (score >= 80) return 'B';
    if (score >= 70) return 'C';
    if (score >= 60) return 'D';
    return 'F';
}

function openUpdateGradeModal() {
    const modal = document.getElementById('updateGradeModal');
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
    
    // Load current grade if student is selected
    const studentSelect = document.getElementById('gradeStudentSelect');
    if (studentSelect && studentSelect.value) {
        loadCurrentGrade(studentSelect.value, document.getElementById('gradeSubject').value);
    }
}

function closeUpdateGradeModal() {
    const modal = document.getElementById('updateGradeModal');
    modal.classList.remove('active');
    document.body.style.overflow = 'auto';
    document.getElementById('updateGradeForm').reset();
    document.getElementById('currentGradeScore').value = '';
}

// Close modal when clicking outside
window.onclick = function(event) {
    const modal = document.getElementById('updateGradeModal');
    if (event.target === modal) {
        closeUpdateGradeModal();
    }
}

async function loadCurrentGrade(studentId, subject) {
    if (!studentId || !subject) {
        document.getElementById('currentGradeScore').value = '';
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/api/user/grades?userId=${studentId}`);
        const result = await response.json();
        
        if (result.success && result.grades) {
            const grade = result.grades.find(g => g.subject === subject);
            if (grade) {
                document.getElementById('currentGradeScore').value = grade.score;
            } else {
                document.getElementById('currentGradeScore').value = 'N/A';
            }
        }
    } catch (error) {
        console.error('Error loading current grade:', error);
    }
}

async function updateStudentGrade(event) {
    event.preventDefault();
    const studentSelect = document.getElementById('gradeStudentSelect');
    const selectedOption = studentSelect.options[studentSelect.selectedIndex];
    const studentId = selectedOption.value;
    const studentName = selectedOption.textContent.split(' (')[0];
    const subject = document.getElementById('gradeSubjectSelect').value;
    const currentScore = parseInt(document.getElementById('currentGradeScore').value) || 0;
    const newScore = parseInt(document.getElementById('gradeScore').value);

    if (!studentId) {
        showModalMessage('Please select a student', 'error');
        return;
    }

    if (newScore < 0 || newScore > 100) {
        showModalMessage('Score must be between 0 and 100', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/api/grades/update`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                studentName: studentId,
                subject: subject,
                oldGrade: currentScore,
                newGrade: newScore,
                notifyStudent: true,
                notifyParent: true
            })
        });

        const result = await response.json();
        if (result.success) {
            showModalMessage(`Grade updated successfully for ${studentName}!`, 'success');
            setTimeout(() => {
                closeUpdateGradeModal();
                if (currentUser && currentUser.roles && currentUser.roles.includes('STUDENT')) {
                    loadStudentData();
                }
            }, 1500);
        } else {
            showModalMessage('Error: ' + result.error, 'error');
        }
    } catch (error) {
        showModalMessage('Error: ' + error.message, 'error');
    }
}

function showModalMessage(message, type) {
    const form = document.getElementById('updateGradeForm');
    let messageDiv = document.getElementById('modalMessage');
    
    if (!messageDiv) {
        messageDiv = document.createElement('div');
        messageDiv.id = 'modalMessage';
        messageDiv.style.marginTop = '15px';
        messageDiv.style.padding = '12px 16px';
        messageDiv.style.borderRadius = '8px';
        messageDiv.style.fontWeight = '500';
        form.appendChild(messageDiv);
    }
    
    messageDiv.textContent = message;
    messageDiv.style.display = 'block';
    
    if (type === 'success') {
        messageDiv.style.background = 'linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%)';
        messageDiv.style.color = '#065f46';
        messageDiv.style.borderLeft = '4px solid #10b981';
    } else {
        messageDiv.style.background = 'linear-gradient(135deg, #fee2e2 0%, #fecaca 100%)';
        messageDiv.style.color = '#991b1b';
        messageDiv.style.borderLeft = '4px solid #ef4444';
    }
    
    setTimeout(() => {
        messageDiv.style.display = 'none';
    }, 3000);
}

async function recordAttendance(event) {
    event.preventDefault();
    const studentSelect = document.getElementById('attStudentSelect');
    const selectedOption = studentSelect.options[studentSelect.selectedIndex];
    const studentId = selectedOption.value;
    const studentName = selectedOption.textContent.split(' (')[0];
    const subject = document.getElementById('attSubjectSelect').value;
    const date = document.getElementById('attDate').value;
    const present = document.getElementById('attPresent').value === 'true';

    if (!studentId) {
        alert('Please select a student');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/api/attendance/record`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                studentId: studentId,
                studentName: studentName,
                subject: subject,
                date: date,
                present: present
            })
        });

        const result = await response.json();
        if (result.success) {
            alert(`Attendance recorded successfully for ${studentName}!`);
            document.getElementById('recordAttendanceForm').reset();
            studentSelect.selectedIndex = 0;
            document.getElementById('attDate').valueAsDate = new Date();
        } else {
            alert('Error: ' + result.error);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

async function registerStudent(event) {
    event.preventDefault();
    const data = {
        id: document.getElementById('adminStudentId').value,
        name: document.getElementById('adminStudentName').value,
        major: document.getElementById('adminStudentMajor').value,
        year: '2024-2025',
        trimester: 'first'
    };

    try {
        const response = await fetch(`${API_BASE}/api/students/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        const result = await response.json();
        if (result.success) {
            alert('Student registered successfully!');
            document.getElementById('adminStudentForm').reset();
        } else {
            alert('Error: ' + result.error);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

function showStaffOnboardMessage(message, type) {
    const form = document.getElementById('adminStaffForm');
    let messageDiv = document.getElementById('staffOnboardMessage');
    
    if (!messageDiv) {
        messageDiv = document.createElement('div');
        messageDiv.id = 'staffOnboardMessage';
        messageDiv.style.marginTop = '15px';
        messageDiv.style.padding = '12px 16px';
        messageDiv.style.borderRadius = '8px';
        messageDiv.style.fontWeight = '500';
        form.appendChild(messageDiv);
    }
    
    messageDiv.textContent = message;
    messageDiv.style.display = 'block';
    
    if (type === 'success') {
        messageDiv.style.background = 'linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%)';
        messageDiv.style.color = '#065f46';
        messageDiv.style.borderLeft = '4px solid #10b981';
    } else {
        messageDiv.style.background = 'linear-gradient(135deg, #fee2e2 0%, #fecaca 100%)';
        messageDiv.style.color = '#991b1b';
        messageDiv.style.borderLeft = '4px solid #ef4444';
    }
    
    setTimeout(() => {
        messageDiv.style.display = 'none';
    }, 3000);
}

async function onboardStaff(event) {
    event.preventDefault();
    const data = {
        id: document.getElementById('adminStaffId').value,
        firstName: document.getElementById('adminStaffFirstName').value,
        lastName: document.getElementById('adminStaffLastName').value,
        department: document.getElementById('adminStaffDept').value,
        position: document.getElementById('adminStaffPosition').value,
        year: '2024-2025',
        trimester: 'first'
    };

    try {
        const response = await fetch(`${API_BASE}/api/staff/onboard`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        const result = await response.json();
        if (result.success) {
            showStaffOnboardMessage('Staff hired successfully!', 'success');
            document.getElementById('adminStaffForm').reset();
            setTimeout(() => {
                loadStaffList();
                showAdminTab('staff');
            }, 1500);
        } else {
            showStaffOnboardMessage('Error: ' + result.error, 'error');
        }
    } catch (error) {
        showStaffOnboardMessage('Error: ' + error.message, 'error');
    }
}

async function runSystemDemo() {
    // System demo has been removed from the dashboard UI.
    // This function is kept as a no-op placeholder in case older HTML still references it.
    return;
}

function logout() {
    localStorage.removeItem('user');
    window.location.href = 'login.html';
}

// Admin functions
async function loadAllUsers() {
    try {
        const response = await fetch(`${API_BASE}/api/users/all`);
        const result = await response.json();
        
        if (result.success) {
            const tbody = document.getElementById('usersTableBody');
            if (!tbody) return;
            tbody.innerHTML = '';
            
            result.users.forEach(user => {
                const row = document.createElement('tr');
                const roles = user.roles ? user.roles.join(', ') : '';
                const majorDept = user.major || user.department || '-';
                row.innerHTML = `
                    <td>${user.userId}</td>
                    <td>${user.firstName} ${user.lastName}</td>
                    <td>${user.email}</td>
                    <td>${roles}</td>
                    <td>${majorDept}</td>
                    <td>
                        <button onclick="editUser('${user.userId}')" class="btn-primary" style="padding: 5px 10px; margin-right: 5px;">Edit</button>
                        <button onclick="deleteUser('${user.userId}')" class="logout-btn" style="padding: 5px 10px;">Delete</button>
                    </td>
                `;
                tbody.appendChild(row);
            });
        }
    } catch (error) {
        console.error('Error loading users:', error);
    }
}

async function loadStaffList() {
    try {
        const response = await fetch(`${API_BASE}/api/staff/all`);
        const result = await response.json();
        
        if (result.success) {
            const tbody = document.getElementById('staffTableBody');
            if (!tbody) return;
            tbody.innerHTML = '';
            
            result.staff.forEach(staff => {
                const row = document.createElement('tr');
                const roles = staff.roles ? staff.roles.join(', ') : '';
                row.innerHTML = `
                    <td>${staff.userId}</td>
                    <td>${staff.firstName} ${staff.lastName}</td>
                    <td>${staff.email}</td>
                    <td>${staff.department || '-'}</td>
                    <td>${staff.position || '-'}</td>
                    <td>${roles}</td>
                    <td>
                        <button onclick="editUser('${staff.userId}')" class="btn-primary" style="padding: 5px 10px; margin-right: 5px;">Edit</button>
                        <button onclick="deleteUser('${staff.userId}')" class="logout-btn" style="padding: 5px 10px;">Delete</button>
                    </td>
                `;
                tbody.appendChild(row);
            });
        }
    } catch (error) {
        console.error('Error loading staff:', error);
    }
}

async function editUser(userId) {
    try {
        const response = await fetch(`${API_BASE}/api/users/all`);
        const result = await response.json();
        
        if (result.success) {
            const user = result.users.find(u => u.userId === userId);
            if (user) {
                document.getElementById('editUserId').value = user.userId;
                document.getElementById('editFirstName').value = user.firstName || '';
                document.getElementById('editLastName').value = user.lastName || '';
                document.getElementById('editEmail').value = user.email || '';
                document.getElementById('editMajor').value = user.major || '';
                document.getElementById('editDepartment').value = user.department || '';
                document.getElementById('editPosition').value = user.position || '';
                
                openEditUserModal();
            }
        }
    } catch (error) {
        console.error('Error loading user data:', error);
        alert('Error loading user data: ' + error.message);
    }
}

function openEditUserModal() {
    const modal = document.getElementById('editUserModal');
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
}

function closeEditUserModal() {
    const modal = document.getElementById('editUserModal');
    modal.classList.remove('active');
    document.body.style.overflow = 'auto';
    document.getElementById('editUserForm').reset();
}

async function updateUserFromModal(event) {
    event.preventDefault();
    const userId = document.getElementById('editUserId').value;
    const firstName = document.getElementById('editFirstName').value;
    const lastName = document.getElementById('editLastName').value;
    const email = document.getElementById('editEmail').value;
    const major = document.getElementById('editMajor').value || null;
    const department = document.getElementById('editDepartment').value || null;
    const position = document.getElementById('editPosition').value || null;
    
    await updateUser(userId, firstName, lastName, email, major, department, position);
}

// Close modal when clicking outside
document.addEventListener('click', function(event) {
    const editModal = document.getElementById('editUserModal');
    if (event.target === editModal) {
        closeEditUserModal();
    }
});

async function updateUser(userId, firstName, lastName, email, major, department, position) {
    try {
        const response = await fetch(`${API_BASE}/api/users/update`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userId: userId,
                firstName: firstName,
                lastName: lastName,
                email: email,
                major: major,
                department: department,
                position: position
            })
        });

        const result = await response.json();
        if (result.success) {
            showEditUserMessage('User updated successfully!', 'success');
            setTimeout(() => {
                closeEditUserModal();
                loadAllUsers();
            }, 1500);
        } else {
            showEditUserMessage('Error: ' + result.error, 'error');
        }
    } catch (error) {
        showEditUserMessage('Error: ' + error.message, 'error');
    }
}

function showEditUserMessage(message, type) {
    const form = document.getElementById('editUserForm');
    let messageDiv = document.getElementById('editUserMessage');
    
    if (!messageDiv) {
        messageDiv = document.createElement('div');
        messageDiv.id = 'editUserMessage';
        messageDiv.style.marginTop = '15px';
        messageDiv.style.padding = '12px 16px';
        messageDiv.style.borderRadius = '8px';
        messageDiv.style.fontWeight = '500';
        form.appendChild(messageDiv);
    }
    
    messageDiv.textContent = message;
    messageDiv.style.display = 'block';
    
    if (type === 'success') {
        messageDiv.style.background = 'linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%)';
        messageDiv.style.color = '#065f46';
        messageDiv.style.borderLeft = '4px solid #10b981';
    } else {
        messageDiv.style.background = 'linear-gradient(135deg, #fee2e2 0%, #fecaca 100%)';
        messageDiv.style.color = '#991b1b';
        messageDiv.style.borderLeft = '4px solid #ef4444';
    }
    
    setTimeout(() => {
        messageDiv.style.display = 'none';
    }, 3000);
}

async function deleteUser(userId) {
    if (!confirm(`Are you sure you want to delete user ${userId}?`)) {
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/api/users/delete`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ userId: userId })
        });

        const result = await response.json();
        if (result.success) {
            alert('User deleted successfully!');
            loadAllUsers();
        } else {
            alert('Error: ' + result.error);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

async function loadNotifications() {
    if (!currentUser || !currentUser.userId) return;
    
    try {
        const response = await fetch(`${API_BASE}/api/notifications?userId=${currentUser.userId}`);
        const result = await response.json();
        
        if (result.success) {
            displayNotifications(result.notifications);
            updateNotificationBadge(result.notifications);
        }
    } catch (error) {
        console.error('Error loading notifications:', error);
    }
}

function displayNotifications(notifications) {
    const container = document.getElementById('notificationsList');
    if (!container) return;
    container.innerHTML = '';
    
    if (notifications.length === 0) {
        container.innerHTML = '<p style="text-align: center; color: #9ca3af; padding: 40px; font-style: italic;">No notifications yet</p>';
        return;
    }
    
    notifications.forEach((notif, index) => {
        const notifDiv = document.createElement('div');
        notifDiv.className = 'card';
        notifDiv.style.marginBottom = '15px';
        notifDiv.style.padding = '20px';
        notifDiv.style.opacity = notif.isRead ? '0.8' : '1';
        notifDiv.style.borderLeft = notif.isRead ? '3px solid #d1d5db' : '5px solid #667eea';
        notifDiv.style.cursor = 'pointer';
        notifDiv.style.transition = 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)';
        notifDiv.style.animation = `fadeIn 0.4s ease-out ${index * 0.05}s both`;
        
        if (!notif.isRead) {
            notifDiv.style.boxShadow = '0 4px 12px rgba(102, 126, 234, 0.2)';
            notifDiv.style.background = 'linear-gradient(135deg, rgba(255, 255, 255, 0.98) 0%, rgba(102, 126, 234, 0.02) 100%)';
        }
        
        const typeColors = {
            'GRADE_UPDATE': 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
            'ATTENDANCE': 'linear-gradient(135deg, #3b82f6 0%, #2563eb 100%)',
            'SYSTEM': 'linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)'
        };
        
        const typeColor = typeColors[notif.type] || 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
        
        notifDiv.innerHTML = `
            <div style="display: flex; justify-content: space-between; align-items: start; gap: 15px;">
                <div style="flex: 1;">
                    <div style="display: flex; align-items: center; gap: 10px; margin-bottom: 8px;">
                        <span style="background: ${typeColor}; color: white; padding: 4px 12px; border-radius: 12px; font-size: 0.75em; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px;">${notif.type.replace('_', ' ')}</span>
                ${!notif.isRead ? '<span style="width: 8px; height: 8px; background: #667eea; border-radius: 50%; display: inline-block; animation: pulse 2s infinite;"></span>' : ''}
                    </div>
                    <p style="margin: 8px 0; color: #1f2937; font-size: 1em; line-height: 1.6; font-weight: 500;">${notif.message}</p>
                    <small style="color: #9ca3af; font-size: 0.85em;">${new Date(notif.createdAt).toLocaleString()}</small>
                </div>
                ${!notif.isRead ? `<button onclick="event.stopPropagation(); markNotificationRead(${notif.id})" class="btn-primary" style="padding: 8px 16px; font-size: 0.9em; white-space: nowrap;">Mark Read</button>` : '<span style="color: #9ca3af; font-size: 0.85em;">Read</span>'}
            </div>
        `;
        
        notifDiv.onclick = () => {
            if (!notif.isRead) {
                markNotificationRead(notif.id);
            }
        };
        
        notifDiv.onmouseenter = () => {
            if (!notif.isRead) {
                notifDiv.style.transform = 'translateX(5px)';
            }
        };
        
        notifDiv.onmouseleave = () => {
            notifDiv.style.transform = 'translateX(0)';
        };
        
        container.appendChild(notifDiv);
    });
    
    // Add pulse animation for unread indicator
    if (!document.getElementById('notificationStyles')) {
        const style = document.createElement('style');
        style.id = 'notificationStyles';
        style.textContent = `
            @keyframes pulse {
                0%, 100% { opacity: 1; transform: scale(1); }
                50% { opacity: 0.5; transform: scale(1.2); }
            }
        `;
        document.head.appendChild(style);
    }
}

function updateNotificationBadge(notifications) {
    const unreadCount = notifications.filter(n => !n.isRead).length;
    const badge = document.getElementById('notificationBadge');
    if (badge) {
        if (unreadCount > 0) {
            badge.textContent = unreadCount;
            badge.style.display = 'inline-block';
            badge.className = 'badge absent';
        } else {
            badge.style.display = 'none';
        }
    }
}

async function markNotificationRead(notificationId) {
    try {
        const response = await fetch(`${API_BASE}/api/notifications/read`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ notificationId: notificationId })
        });

        const result = await response.json();
        if (result.success) {
            loadNotifications();
        }
    } catch (error) {
        console.error('Error marking notification as read:', error);
    }
}

async function loadParentLinkForm() {
    try {
        const [usersResponse, studentsResponse] = await Promise.all([
            fetch(`${API_BASE}/api/users/all`),
            fetch(`${API_BASE}/api/students/all`)
        ]);
        
        const usersResult = await usersResponse.json();
        const studentsResult = await studentsResponse.json();
        
        if (usersResult.success && studentsResult.success) {
            const parentSelect = document.getElementById('parentSelect');
            const studentSelect = document.getElementById('studentSelectForParent');
            
            if (parentSelect) {
                parentSelect.innerHTML = '<option value="">-- Select a parent --</option>';
                usersResult.users.forEach(user => {
                    if (user.roles && user.roles.includes('PARENT')) {
                        const option = document.createElement('option');
                        option.value = user.userId;
                        option.textContent = `${user.firstName} ${user.lastName} (${user.userId})`;
                        parentSelect.appendChild(option);
                    }
                });
            }
            
            if (studentSelect) {
                studentSelect.innerHTML = '<option value="">-- Select a student --</option>';
                studentsResult.students.forEach(student => {
                    const option = document.createElement('option');
                    option.value = student.userId;
                    option.textContent = `${student.firstName} ${student.lastName} (${student.userId})`;
                    studentSelect.appendChild(option);
                });
            }
        }
    } catch (error) {
        console.error('Error loading parent link form:', error);
    }
}

async function linkParentToStudent(event) {
    event.preventDefault();
    const parentUserId = document.getElementById('parentSelect').value;
    const studentUserId = document.getElementById('studentSelectForParent').value;
    
    if (!parentUserId || !studentUserId) {
        alert('Please select both parent and student');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/api/parent/link`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                parentUserId: parentUserId,
                studentUserId: studentUserId
            })
        });

        const result = await response.json();
        if (result.success) {
            alert('Parent linked to student successfully!');
            document.getElementById('linkParentForm').reset();
        } else {
            alert('Error: ' + result.error);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

async function loadParentData() {
    if (!currentUser || !currentUser.userId) return;
    
    try {
        const response = await fetch(`${API_BASE}/api/parent/students?parentUserId=${currentUser.userId}`);
        const result = await response.json();
        
        if (result.success && result.studentIds && result.studentIds.length > 0) {
            const gradesPromises = result.studentIds.map(studentId => 
                fetch(`${API_BASE}/api/user/grades?userId=${studentId}`)
            );
            const attendancePromises = result.studentIds.map(studentId =>
                fetch(`${API_BASE}/api/user/attendance?userId=${studentId}`)
            );
            
            const [gradesResponses, attendanceResponses] = await Promise.all([
                Promise.all(gradesPromises),
                Promise.all(attendancePromises)
            ]);
            const gradesResults = await Promise.all(gradesResponses.map(r => r.json()));
            const attendanceResults = await Promise.all(attendanceResponses.map(r => r.json()));
            
            displayParentChildrenGrades(result.studentIds, gradesResults, attendanceResults);
        } else {
            const container = document.getElementById('parentChildrenGrades');
            if (container) {
                // Make text white for readability on violet background
                container.innerHTML = '<p style="color: white !important; text-shadow: 0 2px 10px rgba(0, 0, 0, 0.5); padding: 20px; text-align: center; font-size: 1.1em; font-weight: 500;">No children linked. Please contact admin to link your account to your child.</p>';
            }
        }
    } catch (error) {
        console.error('Error loading parent data:', error);
        const container = document.getElementById('parentChildrenGrades');
        if (container) {
            // Make text white for readability on violet background
            container.innerHTML = '<p style="color: white !important; text-shadow: 0 2px 10px rgba(0, 0, 0, 0.5); padding: 20px; text-align: center;">Error loading children grades.</p>';
        }
    }
}

function displayParentChildrenGrades(studentIds, gradesResults, attendanceResults) {
    const container = document.getElementById('parentChildrenGrades');
    if (!container) return;
    container.innerHTML = '';
    
    studentIds.forEach((studentId, index) => {
        const gradesData = gradesResults[index];
        const attData = attendanceResults ? attendanceResults[index] : null;
        if (gradesData.success && gradesData.grades && gradesData.grades.length > 0) {
            const childDiv = document.createElement('div');
            childDiv.className = 'card';
            childDiv.style.marginBottom = '20px';
            childDiv.innerHTML = `<h4>Student: ${studentId}</h4>`;
            
            const table = document.createElement('table');
            table.innerHTML = `
                <thead>
                    <tr>
                        <th>Subject</th>
                        <th>Score</th>
                        <th>Grade</th>
                        <th>Status</th>
                        <th>Last Updated</th>
                    </tr>
                </thead>
                <tbody></tbody>
            `;
            
            const tbody = table.querySelector('tbody');
            gradesData.grades.forEach(grade => {
                const row = document.createElement('tr');
                const letterGrade = getLetterGrade(grade.score);
                const status = grade.score >= 60 ? 'PASS' : 'FAIL';
                row.innerHTML = `
                    <td>${grade.subject}</td>
                    <td>${grade.score}</td>
                    <td>${letterGrade}</td>
                    <td><span class="badge ${status === 'PASS' ? 'present' : 'absent'}">${status}</span></td>
                    <td>${new Date(grade.updatedAt).toLocaleDateString()}</td>
                `;
                tbody.appendChild(row);
            });
            
            childDiv.appendChild(table);

            if (attData && attData.success) {
                const attP = document.createElement('p');
                attP.style.marginTop = '10px';
                attP.textContent = `Attendance: ${attData.percentage.toFixed(1)}%`;
                childDiv.appendChild(attP);
            }
            container.appendChild(childDiv);
        }
    });
    
    if (container.innerHTML === '') {
        container.innerHTML = '<p style="color: #4b5563; padding: 20px; text-align: center;">No grades available for your children.</p>';
    }
}

function renderTimetable(entries, tbodyId, includeTeacher) {
    const tbody = document.getElementById(tbodyId);
    if (!tbody) return;
    tbody.innerHTML = '';

    if (!entries || entries.length === 0) {
        const colSpan = includeTeacher ? 5 : 4;
        tbody.innerHTML = `<tr><td colspan="${colSpan}" style="text-align: center;">No timetable entries</td></tr>`;
        return;
    }

    entries.forEach(entry => {
        const row = document.createElement('tr');
        const time = `${entry.startTime || ''} - ${entry.endTime || ''}`.trim();
        if (includeTeacher) {
            row.innerHTML = `
                <td>${entry.dayOfWeek || ''}</td>
                <td>${time}</td>
                <td>${entry.subject || ''}</td>
                <td>${entry.teacher || ''}</td>
                <td>${entry.room || ''}</td>
            `;
        } else {
            row.innerHTML = `
                <td>${entry.dayOfWeek || ''}</td>
                <td>${time}</td>
                <td>${entry.subject || ''}</td>
                <td>${entry.room || ''}</td>
            `;
        }
        tbody.appendChild(row);
    });
}

