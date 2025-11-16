const API_BASE = 'http://localhost:8080';


function showTab(tabName) {
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });

    document.querySelectorAll('.tab-button').forEach(btn => {
        btn.classList.remove('active');
    });

    document.getElementById(tabName).classList.add('active');

    event.target.classList.add('active');
}

async function registerStudent(event) {
    event.preventDefault();
    const resultDiv = document.getElementById('studentResult');
    resultDiv.className = 'result';
    resultDiv.style.display = 'none';

    const data = {
        id: document.getElementById('studentId').value,
        name: document.getElementById('studentName').value,
        major: document.getElementById('studentMajor').value,
        year: document.getElementById('studentYear').value,
        trimester: document.getElementById('studentTrimester').value
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
            resultDiv.className = 'result success';
            resultDiv.innerHTML = `
                <strong>${result.message}</strong>
                <pre>${result.output}</pre>
            `;
            appendOutput(result.output);
        } else {
            resultDiv.className = 'result error';
            resultDiv.innerHTML = `<strong>Error:</strong> ${result.error}`;
        }
    } catch (error) {
        resultDiv.className = 'result error';
        resultDiv.innerHTML = `<strong>Error:</strong> ${error.message}`;
    }
}

async function onboardStaff(event) {
    event.preventDefault();
    const resultDiv = document.getElementById('staffResult');
    resultDiv.className = 'result';
    resultDiv.style.display = 'none';

    const data = {
        id: document.getElementById('staffId').value,
        firstName: document.getElementById('staffFirstName').value,
        lastName: document.getElementById('staffLastName').value,
        department: document.getElementById('staffDept').value,
        position: document.getElementById('staffPosition').value,
        year: document.getElementById('staffYear').value,
        trimester: document.getElementById('staffTrimester').value
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
            resultDiv.className = 'result success';
            resultDiv.innerHTML = `
                <strong>${result.message}</strong>
                <pre>${result.output}</pre>
            `;
            appendOutput(result.output);
        } else {
            resultDiv.className = 'result error';
            resultDiv.innerHTML = `<strong>Error:</strong> ${result.error}`;
        }
    } catch (error) {
        resultDiv.className = 'result error';
        resultDiv.innerHTML = `<strong>Error:</strong> ${error.message}`;
    }
}

async function updateGrade(event) {
    event.preventDefault();
    const resultDiv = document.getElementById('gradeResult');
    resultDiv.className = 'result';
    resultDiv.style.display = 'none';

    const data = {
        studentName: document.getElementById('gradeStudentName').value,
        oldGrade: parseInt(document.getElementById('oldGrade').value),
        newGrade: parseInt(document.getElementById('newGrade').value),
        notifyStudent: document.getElementById('notifyStudent').checked,
        notifyParent: document.getElementById('notifyParent').checked
    };

    try {
        const response = await fetch(`${API_BASE}/api/grades/update`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        const result = await response.json();

        if (result.success) {
            resultDiv.className = 'result success';
            resultDiv.innerHTML = `
                <strong>${result.message}</strong>
                <pre>${result.output}</pre>
            `;
            appendOutput(result.output);
        } else {
            resultDiv.className = 'result error';
            resultDiv.innerHTML = `<strong>Error:</strong> ${result.error}`;
        }
    } catch (error) {
        resultDiv.className = 'result error';
        resultDiv.innerHTML = `<strong>Error:</strong> ${error.message}`;
    }
}

async function calculateAttendance(event) {
    event.preventDefault();
    const resultDiv = document.getElementById('attendanceResult');
    resultDiv.className = 'result';
    resultDiv.style.display = 'none';

    const data = {
        studentName: document.getElementById('attendanceStudentName').value,
        presentDays: parseInt(document.getElementById('presentDays').value),
        totalDays: parseInt(document.getElementById('totalDays').value),
        strategy: document.getElementById('attendanceStrategy').value
    };

    try {
        const response = await fetch(`${API_BASE}/api/attendance/calculate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        const result = await response.json();

        if (result.success) {
            resultDiv.className = 'result success';
            let resultText = '';
            if (result.resultType === 'percentage') {
                resultText = `Attendance: ${result.formatted}`;
            } else {
                resultText = `Status: ${result.formatted}`;
            }
            resultDiv.innerHTML = `
                <strong>${resultText}</strong>
                <pre>${result.output}</pre>
            `;
            appendOutput(result.output);
        } else {
            resultDiv.className = 'result error';
            resultDiv.innerHTML = `<strong>Error:</strong> ${result.error}`;
        }
    } catch (error) {
        resultDiv.className = 'result error';
        resultDiv.innerHTML = `<strong>Error:</strong> ${error.message}`;
    }
}

async function createUser(event) {
    event.preventDefault();
    const resultDiv = document.getElementById('userResult');
    resultDiv.className = 'result';
    resultDiv.style.display = 'none';

    const roles = [];
    if (document.getElementById('roleTeacher').checked) roles.push('teacher');
    if (document.getElementById('roleAdmin').checked) roles.push('admin');
    if (document.getElementById('roleAdvisor').checked) roles.push('advisor');

    if (roles.length === 0) {
        resultDiv.className = 'result error';
        resultDiv.innerHTML = '<strong>Error:</strong> Please select at least one role';
        return;
    }

    const data = { roles };

    try {
        const response = await fetch(`${API_BASE}/api/users/create`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });

        const result = await response.json();

        if (result.success) {
            resultDiv.className = 'result success';
            resultDiv.innerHTML = `
                <strong>${result.message}</strong><br>
                <strong>Description:</strong> ${result.description}<br>
                <strong>Access Level:</strong> ${result.accessLevel}
                <pre>${result.output}</pre>
            `;
            appendOutput(result.output);
        } else {
            resultDiv.className = 'result error';
            resultDiv.innerHTML = `<strong>Error:</strong> ${result.error}`;
        }
    } catch (error) {
        resultDiv.className = 'result error';
        resultDiv.innerHTML = `<strong>Error:</strong> ${error.message}`;
    }
}

async function runDemo() {
    // System demo has been removed from the production UI.
    // This function is kept as a no-op placeholder in case older HTML still references it.
    return;
}

function appendOutput(text) {
    // Output area has been removed from the production UI, so this is now a no-op.
    return;
}

