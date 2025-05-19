// group-admin.js
const jwt = localStorage.getItem('jwt');
if (!jwt) window.location.href = 'login.html';

// Utility to show/hide sections
function showSection(id) {
    document.getElementById('createGroupSection').style.display = 'none';
    document.getElementById('adminAssignSection').style.display = 'none';
    document.getElementById('changeRoleSection').style.display = 'none';
    if (id) document.getElementById(id).style.display = '';
}

// Load all groups and render in table
function loadGroups() {
    fetch('/user-groups', { headers: { 'Authorization': 'Bearer ' + jwt } })
        .then(res => res.json())
        .then(groups => {
            const tbody = document.getElementById('groupTableBody');
            tbody.innerHTML = '';
            groups.forEach(g => {
                const tr = document.createElement('tr');
                // ID
                const tdId = document.createElement('td');
                tdId.textContent = g.id;
                tr.appendChild(tdId);
                // Name
                const tdName = document.createElement('td');
                tdName.textContent = g.name;
                tr.appendChild(tdName);
                // Description
                const tdDesc = document.createElement('td');
                tdDesc.textContent = g.description;
                tr.appendChild(tdDesc);
                // Admin (fetch all admins for the group)
                const tdAdmin = document.createElement('td');
                tdAdmin.textContent = 'Loading...';
                fetch(`/user-groups/${g.id}/admins`, { headers: { 'Authorization': 'Bearer ' + jwt } })
                    .then(res => res.ok ? res.json() : [])
                    .then(admins => {
                        if (admins && admins.length > 0) {
                            tdAdmin.innerHTML = admins.map(a => a.email).join(',<br>');
                        } else {
                            tdAdmin.textContent = 'N/A';
                        }
                    })
                    .catch(() => { tdAdmin.textContent = 'N/A'; });
                tr.appendChild(tdAdmin);
                // Action (Assign Admin button)
                const tdAction = document.createElement('td');
                const btn = document.createElement('button');
                btn.textContent = 'Assign Admin';
                btn.onclick = () => {
                    showAssignAdminForm(g.id);
                };
                tdAction.appendChild(btn);
                tr.appendChild(tdAction);
                tbody.appendChild(tr);
            });
        });
}

// Show assign admin form and populate admin dropdown
function showAssignAdminForm(groupId) {
    document.getElementById('adminGroupId').value = groupId;
    const adminSelect = document.getElementById('adminUserId');
    const assignBtn = document.querySelector('#assignAdminForm button[type="submit"]');
    adminSelect.innerHTML = '<option>Loading...</option>';
    assignBtn.disabled = true;
    fetch('/users?role=ADMIN', { headers: { 'Authorization': 'Bearer ' + jwt } })
        .then(res => res.json())
        .then(users => {
            adminSelect.innerHTML = '';
            if (!users.length) {
                const opt = document.createElement('option');
                opt.value = '';
                opt.textContent = 'No admin available';
                adminSelect.appendChild(opt);
                assignBtn.disabled = true;
            } else {
                users.forEach(u => {
                    const opt = document.createElement('option');
                    opt.value = u.id;
                    opt.textContent = u.username + (u.email ? ' (' + u.email + ')' : '');
                    adminSelect.appendChild(opt);
                });
                assignBtn.disabled = false;
            }
        });
    adminSelect.onchange = function() {
        assignBtn.disabled = !adminSelect.value;
    };
    showSection('adminAssignSection');
}

// Show create group form
const showCreateGroup = document.getElementById('showCreateGroup');
showCreateGroup.onclick = () => showSection('createGroupSection');

document.getElementById('cancelCreateGroup').onclick = () => showSection();
document.getElementById('cancelAssignAdmin').onclick = () => showSection();

// Handle create group
const createGroupForm = document.getElementById('createGroupForm');
createGroupForm.onsubmit = function(e) {
    e.preventDefault();
    const data = {
        name: createGroupForm.groupName.value,
        description: createGroupForm.groupDesc.value
    };
    fetch('/user-groups', {
        method: 'POST',
        headers: {
            'Authorization': 'Bearer ' + jwt,
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(res => {
        if (res.ok) {
            document.getElementById('groupAdminMsg').textContent = 'Group created!';
            showSection();
            loadGroups();
        } else {
            document.getElementById('groupAdminMsg').textContent = 'Failed to create group.';
        }
    });
};

// Handle assign admin
const assignAdminForm = document.getElementById('assignAdminForm');
assignAdminForm.onsubmit = function(e) {
    e.preventDefault();
    const groupId = assignAdminForm.adminGroupId.value;
    const userId = assignAdminForm.adminUserId.value;
    fetch(`/user-groups/${groupId}/assign-admin?userId=${userId}`, {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + jwt }
    })
    .then(res => {
        if (res.ok) {
            document.getElementById('groupAdminMsg').textContent = 'Admin assigned!';
            showSection();
        } else {
            document.getElementById('groupAdminMsg').textContent = 'Failed to assign admin.';
        }
    });
};

// Show/hide change role section for superadmin
fetch('/users/me', { headers: { 'Authorization': 'Bearer ' + jwt } })
    .then(res => res.json())
    .then(user => {
        if (user.role === 'SUPERADMIN') {
            document.getElementById('changeRoleSection').style.display = '';
            // Populate user dropdown
            fetch('/users', { headers: { 'Authorization': 'Bearer ' + jwt } })
                .then(res => res.json())
                .then(users => {
                    const userSelect = document.getElementById('userSelect');
                    userSelect.innerHTML = '';
                    users.forEach(u => {
                        const opt = document.createElement('option');
                        opt.value = u.id;
                        opt.textContent = u.username + (u.email ? ' (' + u.email + ')' : '') + ' [' + u.role + ']';
                        userSelect.appendChild(opt);
                    });
                });
        }
    });

// Handle change role form submit
const changeRoleForm = document.getElementById('changeRoleForm');
if (changeRoleForm) {
    changeRoleForm.onsubmit = function(e) {
        e.preventDefault();
        const userId = document.getElementById('userSelect').value;
        const role = document.getElementById('roleSelect').value;
        fetch(`/users/${userId}/role?role=${role}`, {
            method: 'PUT',
            headers: { 'Authorization': 'Bearer ' + jwt }
        })
        .then(res => {
            if (res.ok) {
                document.getElementById('changeRoleMsg').textContent = 'Role updated!';
            } else {
                document.getElementById('changeRoleMsg').textContent = 'Failed to update role.';
            }
        });
    };
}

// Initial load
loadGroups();
