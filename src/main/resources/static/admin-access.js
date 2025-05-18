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
            // For each group, load files
            const tbody = document.getElementById('adminFileTableBody');
            tbody.innerHTML = '';
            user.groupIds.forEach(groupId => {
                fetch(`/files?groupId=${groupId}`, { headers: { 'Authorization': 'Bearer ' + jwt } })
                    .then(res => res.json())
                    .then(files => {
                        files.forEach(f => {
                            const tr = document.createElement('tr');
                            tr.innerHTML = `
                                <td>${f.id}</td>
                                <td>${f.filename}</td>
                                <td>${f.uploadedBy}</td>
                                <td>${f.groupId}</td>
                                <td><button onclick="grantAccess(${f.id})">Grant Access</button></td>
                            `;
                            tbody.appendChild(tr);
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

// Placeholder for grant access logic
function grantAccess(fileId) {
    alert('Grant access logic for file ID ' + fileId + ' goes here.');
}

// Initial load
loadAdminFiles();
loadAdminUsers();
loadAccessRequests();
