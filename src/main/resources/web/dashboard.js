const API_BASE = 'http://localhost:8080';

let currentUser = null;

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
});

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
    } else if (roles.includes('TEACHER') || roles.includes('ADVISOR')) {
        showTeacherDashboard();
    } else if (roles.includes('ADMIN')) {
        showAdminDashboard();
    } else {
        // Default to student view
        showStudentDashboard();
        loadStudentData();
    }
}

function showStudentDashboard() {
    document.getElementById('studentDashboard').classList.add('active');
    document.getElementById('teacherDashboard').classList.remove('active');
    document.getElementById('adminDashboard').classList.remove('active');
}

function showTeacherDashboard() {
    document.getElementById('studentDashboard').classList.remove('active');
    document.getElementById('teacherDashboard').classList.add('active');
    document.getElementById('adminDashboard').classList.remove('active');
}

function showAdminDashboard() {
    document.getElementById('studentDashboard').classList.remove('active');
    document.getElementById('teacherDashboard').classList.remove('active');
    document.getElementById('adminDashboard').classList.add('active');
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

function showAdminTab(tabName) {
    document.querySelectorAll('#adminDashboard .tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    document.querySelectorAll('#adminDashboard .tab-button').forEach(btn => {
        btn.classList.remove('active');
    });
    
    document.getElementById('admin' + tabName.charAt(0).toUpperCase() + tabName.slice(1)).classList.add('active');
    event.target.classList.add('active');
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
    } catch (error) {
        console.error('Error loading student data:', error);
    }
}

function displayGrades(grades) {
    const tbody = document.getElementById('gradesBody');
    tbody.innerHTML = '';

    if (grades.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" style="text-align: center;">No grades available</td></tr>';
        return;
    }

    grades.forEach(grade => {
        const row = document.createElement('tr');
        const letterGrade = getLetterGrade(grade.score);
        row.innerHTML = `
            <td>${grade.subject}</td>
            <td>${grade.score}</td>
            <td>${letterGrade}</td>
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

async function updateStudentGrade(event) {
    event.preventDefault();
    const studentId = document.getElementById('gradeStudentId').value;
    const subject = document.getElementById('gradeSubject').value;
    const score = parseInt(document.getElementById('gradeScore').value);

    try {
        const response = await fetch(`${API_BASE}/api/grades/update`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                studentName: studentId,
                oldGrade: score - 1,
                newGrade: score,
                notifyStudent: true,
                notifyParent: true
            })
        });

        const result = await response.json();
        if (result.success) {
            alert('Grade updated successfully!');
            document.getElementById('updateGradeForm').reset();
        } else {
            alert('Error: ' + result.error);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

async function recordAttendance(event) {
    event.preventDefault();
    const studentId = document.getElementById('attStudentId').value;
    const studentName = document.getElementById('attStudentName').value;
    const subject = document.getElementById('attSubject').value;
    const date = document.getElementById('attDate').value;
    const present = document.getElementById('attPresent').checked;

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
            alert('Attendance recorded successfully!');
            document.getElementById('recordAttendanceForm').reset();
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

async function onboardStaff(event) {
    event.preventDefault();
    const data = {
        id: document.getElementById('adminStaffId').value,
        name: document.getElementById('adminStaffName').value,
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
            alert('Staff onboarded successfully!');
            document.getElementById('adminStaffForm').reset();
        } else {
            alert('Error: ' + result.error);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

async function runSystemDemo() {
    const outputDiv = document.getElementById('demoOutput');
    outputDiv.className = 'result success';
    outputDiv.innerHTML = '<strong>Running demo...</strong>';
    outputDiv.style.display = 'block';

    try {
        const response = await fetch(`${API_BASE}/api/demo/run`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        const result = await response.json();
        if (result.success) {
            outputDiv.innerHTML = `
                <strong>✓ ${result.message}</strong>
                <pre>${result.output}</pre>
            `;
        } else {
            outputDiv.className = 'result error';
            outputDiv.innerHTML = `<strong>✗ Error:</strong> ${result.error}`;
        }
    } catch (error) {
        outputDiv.className = 'result error';
        outputDiv.innerHTML = `<strong>✗ Error:</strong> ${error.message}`;
    }
}

function logout() {
    localStorage.removeItem('user');
    window.location.href = 'login.html';
}

