// Dashboard logic: show/hide sections based on role, handle upload/download
const jwt = localStorage.getItem('jwt');
if (!jwt) window.location.href = 'login.html';

// Fetch user info to determine role
fetch('/users/me', { headers: { 'Authorization': 'Bearer ' + jwt } })
    .then(res => res.json())
    .then(user => {
        showRoleSections(user.role);
        if (user.role !== 'SUPERADMIN' && (!user.groupIds || user.groupIds.length === 0)) {
            document.getElementById('fileSection').style.display = 'none';
            const groupRequestSection = document.getElementById('groupRequestSection');
            groupRequestSection.style.display = '';
            loadAvailableGroups();
            // Check if user already has a pending or approved request
            fetch('/api/access-requests/user/' + user.id, { headers: { 'Authorization': 'Bearer ' + jwt } })
                .then(res => res.json())
                .then(function(requests) {
                    let hasActiveRequest = false;
                    if (requests && requests.length) {
                        for (var i = 0; i < requests.length; i++) {
                            if (requests[i].status === 'PENDING' || requests[i].status === 'APPROVED') {
                                hasActiveRequest = true;
                                break;
                            }
                        }
                    }
                    if (hasActiveRequest) {
                        document.getElementById('groupRequestForm').style.display = 'none';
                        document.getElementById('groupRequestMsg').innerText = 'You have already requested to join a group.';
                    } else {
                        document.getElementById('groupRequestForm').style.display = '';
                        document.getElementById('groupRequestMsg').innerText = '';
                        document.getElementById('groupRequestForm').onsubmit = function(e) {
                            e.preventDefault();
                            const groupId = document.getElementById('groupSelect').value;
                            fetch('/api/access-requests?groupId=' + groupId + '&userId=' + user.id, {
                                method: 'POST',
                                headers: { 'Authorization': 'Bearer ' + jwt },
                            })
                            .then(res => res.json())
                            .then(data => {
                                document.getElementById('groupRequestMsg').innerText = 'Request submitted!';
                                loadUserRequests(user.id);
                                document.getElementById('groupRequestForm').style.display = 'none';
                            })
                            .catch(() => {
                                document.getElementById('groupRequestMsg').innerText = 'Failed to submit request.';
                            });
                        };
                    }
                    loadUserRequests(user.id);
                });
            return;
        }
        loadFiles();
    })
    .catch(() => window.location.href = 'login.html');

function showRoleSections(role) {
    const roleSection = document.getElementById('roleSection');
    if (role === 'SUPERADMIN') {
        roleSection.innerHTML = `<div class='role-section'><h3>Superadmin Panel</h3>
            <a href='group-admin.html'>Manage Groups & Admins</a></div>`;
    } else if (role === 'ADMIN') {
        roleSection.innerHTML = `<div class='role-section'><h3>Admin Panel</h3>
            <a href='admin-access.html'>Manage File Access & Users</a></div>`;
    } else {
        roleSection.innerHTML = '';
    }
}

// File upload
const uploadForm = document.getElementById('uploadForm');
uploadForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const file = document.getElementById('fileInput').files[0];
    const formData = new FormData();
    formData.append('file', file);
    formData.append('mode', 'SINGLE');
    // Use user's group and id if available
    let userGroupId = 1;
    let uploadedBy = 1;
    try {
        const userRes = await fetch('/users/me', { headers: { 'Authorization': 'Bearer ' + jwt } });
        if (userRes.ok) {
            const user = await userRes.json();
            if (user.groupIds && user.groupIds.length > 0) userGroupId = user.groupIds[0];
            if (user.id) uploadedBy = user.id;
        }
    } catch (err) { console.log('User fetch error', err); }
    formData.append('userGroupId', userGroupId);
    formData.append('fileDto', JSON.stringify({
        description: uploadForm.description.value,
        fileType: uploadForm.fileType.value,
        clickLocation: uploadForm.clickLocation.value,
        clickTime: uploadForm.clickTime.value,
        occasion: uploadForm.occasion.value,
        uploadedBy: uploadedBy
    }));
    console.log('Uploading with userGroupId:', userGroupId, 'uploadedBy:', uploadedBy);
    const res = await fetch('/files/upload', {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + jwt },
        body: formData
    });
    document.getElementById('uploadMsg').innerText = res.ok ? 'Upload successful' : 'Upload failed';
    if (res.ok) loadFiles();
});

// Load files for download
function loadFiles() {
    let currentUser = null;
    const groupAdminCache = {};
    async function isUserGroupAdmin(userId, groupId) {
        if (!groupId) return false;
        if (groupAdminCache[groupId]) {
            return groupAdminCache[groupId].includes(userId);
        }
        try {
            const res = await fetch(`/user-groups/${groupId}/admins`, { headers: { 'Authorization': 'Bearer ' + jwt } });
            if (!res.ok) return false;
            const admins = await res.json();
            const adminIds = admins.map(a => a.id);
            groupAdminCache[groupId] = adminIds;
            return adminIds.includes(userId);
        } catch {
            return false;
        }
    }
    fetch('/users/me', { headers: { 'Authorization': 'Bearer ' + jwt } })
        .then(res => res.json())
        .then(user => {
            currentUser = user;
            return fetch('/files', { headers: { 'Authorization': 'Bearer ' + jwt } });
        })
        .then(res => res.json())
        .then(files => {
            const tbody = document.getElementById('fileTableBody');
            tbody.innerHTML = '';
            files.forEach(async f => {
                const userIsSuperadmin = currentUser.role === 'SUPERADMIN';
                const userUploaded = f.uploadedBy === currentUser.id;
                const userInGroup = currentUser.groupIds && currentUser.groupIds.includes(f.userGroupId);
                let userIsGroupAdmin = false;
                if (userInGroup && !userIsSuperadmin && !userUploaded) {
                    userIsGroupAdmin = await isUserGroupAdmin(currentUser.id, f.userGroupId);
                }
                if (userIsSuperadmin || userUploaded || userIsGroupAdmin) {
                    // Use template literal for correct escaping
                    addFileRow(f, `<button onclick="downloadFile(${f.id}, '${(f.filename || '').replace(/'/g, "\\'")}' )">Download</button>`);
                } else {
                    // For others, check access request status for this file/user
                    fetch(`/access/requests?fileId=${f.id}&requestorId=${currentUser.id}`, { headers: { 'Authorization': 'Bearer ' + jwt } })
                        .then(res => res.json())
                        .then(requests => {
                            let actionCell = '';
                            const req = Array.isArray(requests) ? requests.find(r => r.fileId === f.id && r.requestorId === currentUser.id) : null;
                            if (req && req.status === 'APPROVED') {
                                actionCell = `<button onclick="downloadFile(${f.id}, '${f.filename || ''}')">Download</button>`;
                            } else if (req && req.status === 'PENDING') {
                                actionCell = `<button disabled>Requested</button>`;
                            } else if (req && req.status === 'REJECTED') {
                                actionCell = `<button disabled>Rejected</button>`;
                            } else {
                                actionCell = `<button onclick="requestFileAccess(${f.id}, this)">Request Access</button>`;
                            }
                            addFileRow(f, actionCell);
                        });
                }
            });
        });
}

function addFileRow(f, actionCell) {
    const tbody = document.getElementById('fileTableBody');
    const tr = document.createElement('tr');
    tr.innerHTML = `
        <td>${f.filename || ''}</td>
        <td>${f.description || ''}</td>
        <td>${f.occasion || ''}</td>
        <td>${f.userGroupId || ''}</td>
        <td>${f.clickTime || ''}</td>
        <td>${f.clickLocation || ''}</td>
        <td>${actionCell}</td>
    `;
    tbody.appendChild(tr);
}

window.downloadFile = function(id, filename) {
    fetch(`/files/download/${id}`, {
        headers: { 'Authorization': 'Bearer ' + jwt }
    })
    .then(res => {
        if (!res.ok) throw new Error('No access or file not found');
        return res.blob();
    })
    .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        a.remove();
    })
    .catch(() => alert('Download failed or no access'));
}

window.requestFileAccess = function(fileId, btn) {
    btn.disabled = true;
    btn.textContent = 'Requesting...';
    fetch(`/access/request?fileId=${fileId}`, {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + jwt }
    })
    .then(res => {
        if (res.ok) {
            btn.textContent = 'Requested';
        } else {
            btn.textContent = 'Request Access';
            btn.disabled = false;
            alert('Failed to request access.');
        }
    })
    .catch(() => {
        btn.textContent = 'Request Access';
        btn.disabled = false;
        alert('Failed to request access.');
    });
}

function loadAvailableGroups() {
    fetch('/user-groups', { headers: { 'Authorization': 'Bearer ' + jwt } })
        .then(res => res.json())
        .then(groups => {
            const select = document.getElementById('groupSelect');
            select.innerHTML = '';
            groups.forEach(g => {
                const opt = document.createElement('option');
                opt.value = g.id;
                opt.textContent = g.name;
                select.appendChild(opt);
            });
        });
}

function loadUserRequests(userId) {
    fetch('/api/access-requests/user/' + userId, { headers: { 'Authorization': 'Bearer ' + jwt } })
        .then(res => res.json())
        .then(function(requests) {
            var statusDiv = document.getElementById('groupRequestStatus');
            if (!requests.length) {
                statusDiv.innerHTML = '<em>No requests yet.</em>';
                return;
            }
            var html = '<ul>';
            for (var i = 0; i < requests.length; i++) {
                var r = requests[i];
                html += '<li>Group: ' + (r.userGroupId || r.groupId) + ' | Status: ' + r.status + (r.reviewedAt ? ' (Reviewed)' : '') + '</li>';
            }
            html += '</ul>';
            statusDiv.innerHTML = html;
        });
}

// Show password form only when button is clicked
const showPasswordBtn = document.getElementById('showPasswordBtn');
const passwordSection = document.getElementById('passwordSection');
if (showPasswordBtn && passwordSection) {
    showPasswordBtn.addEventListener('click', () => {
        passwordSection.style.display = '';
        showPasswordBtn.style.display = 'none';
    });
}

// Password update logic
const passwordForm = document.getElementById('passwordForm');
if (passwordForm) {
    passwordForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const oldPassword = document.getElementById('oldPassword').value;
        const newPassword = document.getElementById('newPassword').value;
        let username = null;
        try {
            const userRes = await fetch('/users/me', { headers: { 'Authorization': 'Bearer ' + jwt } });
            if (userRes.ok) {
                const user = await userRes.json();
                username = user.username;
            }
        } catch (err) { }
        if (!username) {
            document.getElementById('passwordMsg').innerText = 'Could not determine user.';
            return;
        }
        const params = new URLSearchParams();
        params.append('username', username);
        params.append('oldPassword', oldPassword);
        params.append('newPassword', newPassword);
        const res = await fetch('/auth/update-password', {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + jwt, 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params
        });
        const msgDiv = document.getElementById('passwordMsg');
        if (res.ok) {
            msgDiv.innerText = 'Password updated successfully.';
            passwordForm.reset();
        } else {
            const data = await res.json().catch(() => ({}));
            msgDiv.innerText = data.message || 'Failed to update password.';
        }
    });
}

// Add logout button logic
const logoutBtn = document.getElementById('logoutBtn');
if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
        localStorage.removeItem('jwt');
        window.location.href = 'login.html';
    });
}
