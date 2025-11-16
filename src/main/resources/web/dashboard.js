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
    loadStudentsForDropdowns();
});

let allStudents = [];

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
    
    if (gradeSelect) {
        gradeSelect.innerHTML = '<option value="">-- Select a student --</option>';
        allStudents.forEach(student => {
            const option = document.createElement('option');
            option.value = student.userId;
            option.textContent = `${student.firstName} ${student.lastName} (${student.userId})`;
            option.dataset.firstName = student.firstName;
            option.dataset.lastName = student.lastName;
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
            attSelect.appendChild(option);
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
    } else if (roles.includes('TEACHER') || roles.includes('ADVISOR')) {
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

function showStudentDashboard() {
    document.getElementById('studentDashboard').classList.add('active');
    document.getElementById('teacherDashboard').classList.remove('active');
    document.getElementById('adminDashboard').classList.remove('active');
}

function showTeacherDashboard() {
    document.getElementById('studentDashboard').classList.remove('active');
    document.getElementById('teacherDashboard').classList.add('active');
    document.getElementById('adminDashboard').classList.remove('active');
    const dateInput = document.getElementById('attDate');
    if (dateInput) {
        dateInput.valueAsDate = new Date();
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
    document.getElementById('teacherDashboard').classList.remove('active');
    document.getElementById('parentDashboard').classList.remove('active');
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
    const studentSelect = document.getElementById('gradeStudentSelect');
    const selectedOption = studentSelect.options[studentSelect.selectedIndex];
    const studentId = selectedOption.value;
    const studentName = selectedOption.textContent.split(' (')[0];
    const subject = document.getElementById('gradeSubject').value;
    const score = parseInt(document.getElementById('gradeScore').value);

    if (!studentId) {
        alert('Please select a student');
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
                oldGrade: score - 1,
                newGrade: score,
                notifyStudent: true,
                notifyParent: true
            })
        });

        const result = await response.json();
        if (result.success) {
            alert(`Grade updated successfully for ${studentName}!`);
            document.getElementById('updateGradeForm').reset();
            studentSelect.selectedIndex = 0;
        } else {
            alert('Error: ' + result.error);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
}

async function recordAttendance(event) {
    event.preventDefault();
    const studentSelect = document.getElementById('attStudentSelect');
    const selectedOption = studentSelect.options[studentSelect.selectedIndex];
    const studentId = selectedOption.value;
    const studentName = selectedOption.textContent.split(' (')[0];
    const subject = document.getElementById('attSubject').value;
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
                `;
                tbody.appendChild(row);
            });
        }
    } catch (error) {
        console.error('Error loading staff:', error);
    }
}

function editUser(userId) {
    const newFirstName = prompt('Enter new first name:');
    const newLastName = prompt('Enter new last name:');
    const newEmail = prompt('Enter new email:');
    
    if (newFirstName && newLastName && newEmail) {
        updateUser(userId, newFirstName, newLastName, newEmail, null, null, null);
    }
}

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
            alert('User updated successfully!');
            loadAllUsers();
        } else {
            alert('Error: ' + result.error);
        }
    } catch (error) {
        alert('Error: ' + error.message);
    }
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
                ${!notif.isRead ? `<button onclick="event.stopPropagation(); markNotificationRead(${notif.id})" class="btn-primary" style="padding: 8px 16px; font-size: 0.9em; white-space: nowrap;">Mark Read</button>` : '<span style="color: #9ca3af; font-size: 0.85em;">✓ Read</span>'}
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
            
            const gradesResponses = await Promise.all(gradesPromises);
            const gradesResults = await Promise.all(gradesResponses.map(r => r.json()));
            
            displayParentChildrenGrades(result.studentIds, gradesResults);
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

function displayParentChildrenGrades(studentIds, gradesResults) {
    const container = document.getElementById('parentChildrenGrades');
    if (!container) return;
    container.innerHTML = '';
    
    studentIds.forEach((studentId, index) => {
        const gradesData = gradesResults[index];
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
                        <th>Last Updated</th>
                    </tr>
                </thead>
                <tbody></tbody>
            `;
            
            const tbody = table.querySelector('tbody');
            gradesData.grades.forEach(grade => {
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
            
            childDiv.appendChild(table);
            container.appendChild(childDiv);
        }
    });
    
    if (container.innerHTML === '') {
        container.innerHTML = '<p style="color: #4b5563; padding: 20px; text-align: center;">No grades available for your children.</p>';
    }
}

