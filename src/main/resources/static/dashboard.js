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
    formData.append('userGroupId', 1); // TODO: select user group
    formData.append('fileDto', JSON.stringify({
        description: uploadForm.description.value,
        fileType: uploadForm.fileType.value,
        clickLocation: uploadForm.clickLocation.value,
        clickTime: uploadForm.clickTime.value,
        occasion: uploadForm.occasion.value,
        uploadedBy: 1 // TODO: set real user id
    }));
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
    fetch('/files', { headers: { 'Authorization': 'Bearer ' + jwt } })
        .then(res => res.json())
        .then(files => {
            const fileList = document.getElementById('fileList');
            fileList.innerHTML = files.map(f =>
                `<div>${f.filename} <button onclick="downloadFile(${f.id}, '${f.filename}')">Download</button></div>`
            ).join('');
        });
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
