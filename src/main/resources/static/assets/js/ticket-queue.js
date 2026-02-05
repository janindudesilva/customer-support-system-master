document.addEventListener('DOMContentLoaded', function() {
    initializeTabs();
    setupDropdowns();
});

// Initialize tab functionality
function initializeTabs() {
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(button => {
        button.addEventListener('click', function(event) {
            showTab(this.getAttribute('onclick').match(/'([^']+)'/)[1]);
            event.preventDefault();
        });
    });
}

// Setup dropdowns
function setupDropdowns() {
    const dropdowns = document.querySelectorAll('.dropdown-btn');
    dropdowns.forEach(dropdown => {
        dropdown.addEventListener('click', function() {
            this.nextElementSibling.classList.toggle('show');
        });
    });

    // Close dropdowns when clicking outside
    window.addEventListener('click', function(event) {
        if (!event.target.matches('.dropdown-btn')) {
            const dropdownContents = document.querySelectorAll('.dropdown-content');
            dropdownContents.forEach(content => {
                if (content.classList.contains('show')) {
                    content.classList.remove('show');
                }
            });
        }
    });
}

function showTab(tabId) {
    // Hide all tab contents
    const tabContents = document.querySelectorAll('.tab-content');
    tabContents.forEach(content => {
        content.classList.remove('active');
    });
    
    // Show the selected tab content
    document.getElementById(tabId).classList.add('active');
    
    // Update active tab button
    const tabButtons = document.querySelectorAll('.tab-btn');
    tabButtons.forEach(button => {
        button.classList.remove('active');
    });
    event.currentTarget.classList.add('active');
}

function filterByStatus() {
    const status = document.getElementById('statusFilter').value;
    // Redirect with filter parameter
    window.location.href = '/agent/ticket-queue?status=' + status;
}

function filterByPriority() {
    const priority = document.getElementById('priorityFilter').value;
    // Redirect with filter parameter
    window.location.href = '/agent/ticket-queue?priority=' + priority;
}

function assignTicket(ticketId) {
    if (confirm('Are you sure you want to assign this ticket to yourself?')) {
        fetch(`/api/agents/tickets/${ticketId}/claim`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to assign ticket');
            }
            return response.json();
        })
        .then(data => {
            // Refresh the page
            window.location.reload();
        })
        .catch(error => {
            console.error('Error assigning ticket:', error);
            alert('Failed to assign ticket. Please try again.');
        });
    }
}

function updateStatus(ticketId, status) {
    fetch(`/api/agents/tickets/${ticketId}/status?status=${status}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to update status');
        }
        return response.json();
    })
    .then(data => {
        // Refresh the page
        window.location.reload();
    })
    .catch(error => {
        console.error('Error updating status:', error);
        alert('Failed to update status. Please try again.');
    });
}

function updatePriority(ticketId, priority) {
    fetch(`/api/agents/tickets/${ticketId}/priority?priority=${priority}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to update priority');
        }
        return response.json();
    })
    .then(data => {
        // Refresh the page
        window.location.reload();
    })
    .catch(error => {
        console.error('Error updating priority:', error);
        alert('Failed to update priority. Please try again.');
    });
}