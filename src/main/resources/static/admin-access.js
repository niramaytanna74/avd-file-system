// admin-access.js
const jwt = localStorage.getItem('jwt');
if (!jwt) window.location.href = 'login.html';

// Load files in admin's groups
function loadAdminFiles() {
    fetch('/users/me', { headers: { 'Authorization': 'Bearer ' + jwt } })
        .then(res => res.json())
        .then(user => {
            if (!user.groupIds || user.groupIds.length === 0) {
                document.getElementById('adminAccessMsg').textContent = 'You are not an admin of any group.';
                return;
            }
            const tbody = document.getElementById('adminFileTableBody');
            tbody.innerHTML = '';
            user.groupIds.forEach(groupId => {
                // Fetch group info for name
                fetch(`/user-groups/${groupId}`, { headers: { 'Authorization': 'Bearer ' + jwt } })
                    .then(res => res.json())
                    .then(group => {
                        fetch(`/files?groupId=${groupId}`, { headers: { 'Authorization': 'Bearer ' + jwt } })
                            .then(res => res.json())
                            .then(files => {
                                files.forEach(f => {
                                    const tr = document.createElement('tr');
                                    tr.innerHTML = `
                                        <td>${f.id || ''}</td>
                                        <td>${f.filename || ''}</td>
                                        <td>${f.userGroupId || ''}</td>
                                        <td>${f.description || ''}</td>
                                        <td>${f.clickLocation || ''}</td>
                                        <td>${f.clickTime || ''}</td>
                                        <td>${f.occasion || ''}</td>
                                        <td>${f.uploadedBy || ''}</td>
                                        <td><button onclick="downloadFile(${f.id}, '${f.filename || ''}')">Download</button></td>
                                    `;
                                    tbody.appendChild(tr);
                                });
                            });
                    });
            });
        });
}

// Load users in admin's groups
function loadAdminUsers() {
    fetch('/users/me', { headers: { 'Authorization': 'Bearer ' + jwt } })
        .then(res => res.json())
        .then(user => {
            if (!user.groupIds || user.groupIds.length === 0) {
                return;
            }
            const tbody = document.getElementById('adminUserTableBody');
            tbody.innerHTML = '';
            user.groupIds.forEach(groupId => {
                fetch(`/users/user-groups/${groupId}/users`, { headers: { 'Authorization': 'Bearer ' + jwt } })
                    .then(res => res.json())
                    .then(users => {
                        users.forEach(u => {
                            const tr = document.createElement('tr');
                            tr.innerHTML = `
                                <td>${u.id}</td>
                                <td>${u.username}</td>
                                <td>${u.email}</td>
                                <td>${groupId}</td>
                                <td>${u.role}</td>
                            `;
                            tbody.appendChild(tr);
                        });
                    });
            });
        });
}

// Load pending group join requests for admin's groups
function loadAccessRequests() {
    fetch('/users/me', { headers: { 'Authorization': 'Bearer ' + jwt } })
        .then(res => res.json())
        .then(user => {
            if (!user.groupIds || user.groupIds.length === 0) return;
            const tbody = document.getElementById('accessRequestsTableBody');
            tbody.innerHTML = '';
            user.groupIds.forEach(function(groupId) {
                fetch(`/api/access-requests/group/${groupId}/pending`, { headers: { 'Authorization': 'Bearer ' + jwt } })
                    .then(res => res.json())
                    .then(function(requests) {
                        requests.forEach(function(r) {
                            const tr = document.createElement('tr');
                            tr.innerHTML = `
                                <td>${r.id}</td>
                                <td>${r.requestorId}</td>
                                <td>${r.userGroupId}</td>
                                <td>${r.status}</td>
                                <td>
                                    <button onclick="approveRequest(${r.id}, ${user.id})">Approve</button>
                                    <button onclick="rejectRequest(${r.id}, ${user.id})">Reject</button>
                                </td>
                            `;
                            tbody.appendChild(tr);
                        });
                    });
            });
        });
}

// Load pending file access requests for admin's groups
function loadFileAccessRequests() {
    fetch('/users/me', { headers: { 'Authorization': 'Bearer ' + jwt } })
        .then(res => res.json())
        .then(user => {
            if (!user.groupIds || user.groupIds.length === 0) return;
            const tbody = document.getElementById('fileAccessRequestsTableBody');
            tbody.innerHTML = '';
            user.groupIds.forEach(function(groupId) {
                fetch(`/access/requests?groupId=${groupId}`, { headers: { 'Authorization': 'Bearer ' + jwt } })
                    .then(res => res.json())
                    .then(function(requests) {
                        requests.forEach(function(r) {
                            const tr = document.createElement('tr');
                            let actionButtons = '';
                            if (r.status === 'APPROVED' || r.status === 'REJECTED') {
                                actionButtons = `
                                    <button disabled>Approve</button>
                                    <button disabled>Reject</button>
                                `;
                            } else {
                                actionButtons = `
                                    <button onclick="approveFileAccessRequest(${r.id})">Approve</button>
                                    <button onclick="rejectFileAccessRequest(${r.id})">Reject</button>
                                `;
                            }
                            tr.innerHTML = `
                                <td>${r.id}</td>
                                <td>${r.fileName || r.fileId}</td>
                                <td>${r.requestorName || r.requestorId}</td>
                                <td>${r.status}</td>
                                <td>${actionButtons}</td>
                            `;
                            tbody.appendChild(tr);
                        });
                    });
            });
        });
}

// Load all file access requests for superadmin
function loadSuperadminFileAccessRequests() {
    fetch('/users/me', { headers: { 'Authorization': 'Bearer ' + jwt } })
        .then(res => res.json())
        .then(user => {
            if (user.role !== 'SUPERADMIN') return;
            const tbody = document.getElementById('fileAccessRequestsTableBody');
            tbody.innerHTML = '';
            fetch('/access/requests', { headers: { 'Authorization': 'Bearer ' + jwt } })
                .then(res => res.json())
                .then(function(requests) {
                    requests.forEach(function(r) {
                        const tr = document.createElement('tr');
                        let actionButtons = '';
                        if (r.status === 'APPROVED' || r.status === 'REJECTED') {
                            actionButtons = `
                                <button disabled>Approve</button>
                                <button disabled>Reject</button>
                            `;
                        } else {
                            actionButtons = `
                                <button onclick="approveFileAccessRequest(${r.id})">Approve</button>
                                <button onclick="rejectFileAccessRequest(${r.id})">Reject</button>
                            `;
                        }
                        tr.innerHTML = `
                            <td>${r.id}</td>
                            <td>${r.fileName || r.fileId}</td>
                            <td>${r.requestorName || r.requestorId}</td>
                            <td>${r.status}</td>
                            <td>${r.requestedAt || ''}</td>
                            <td>${actionButtons}</td>
                        `;
                        tbody.appendChild(tr);
                    });
                });
        });
}

window.approveRequest = function(requestId, adminId) {
    fetch(`/api/access-requests/${requestId}/approve?adminId=${adminId}`, {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + jwt }
    })
    .then(res => res.json())
    .then(function() {
        loadAccessRequests();
        loadAdminUsers(); // Refresh users table after approval
    });
};

window.rejectRequest = function(requestId, adminId) {
    fetch(`/api/access-requests/${requestId}/reject?adminId=${adminId}`, {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + jwt }
    })
    .then(res => res.json())
    .then(function() {
        loadAccessRequests();
        loadAdminUsers(); // Refresh users table after rejection (in case of status change)
    });
};

window.approveFileAccessRequest = function(requestId) {
    fetch(`/access/requests/${requestId}/approve`, {
        method: 'PUT',
        headers: { 'Authorization': 'Bearer ' + jwt }
    })
    .then(res => res.json())
    .then(function() {
        loadFileAccessRequests();
    });
};

window.rejectFileAccessRequest = function(requestId) {
    fetch(`/access/requests/${requestId}/reject`, {
        method: 'PUT',
        headers: { 'Authorization': 'Bearer ' + jwt }
    })
    .then(res => res.json())
    .then(function() {
        loadFileAccessRequests();
    });
};

// Placeholder for grant access logic
function grantAccess(fileId) {
    alert('Grant access logic for file ID ' + fileId + ' goes here.');
}

// Add downloadFile function
downloadFile = function(fileId, filename) {
    fetch(`/files/${fileId}/download`, {
        headers: { 'Authorization': 'Bearer ' + jwt }
    })
    .then(response => {
        if (!response.ok) throw new Error('Download failed');
        return response.blob();
    })
    .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename || 'downloaded_file';
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
    })
    .catch(() => alert('File download failed.'));
}

// Initial load
fetch('/users/me', { headers: { 'Authorization': 'Bearer ' + jwt } })
    .then(res => res.json())
    .then(user => {
        if (user.role === 'SUPERADMIN') {
            loadSuperadminFileAccessRequests();
        } else {
            loadFileAccessRequests();
        }
    });
loadAdminFiles();
loadAdminUsers();
loadAccessRequests();
