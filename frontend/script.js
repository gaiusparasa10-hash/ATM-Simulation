/* ========================================================
   ATM SIMULATION SYSTEM - VANILLA JAVASCRIPT FRONTEND LOGIC
   Handles REST API communications with the Java HTTP Backend
   ======================================================== */

const API_BASE = '/api';

// State management
let currentUser = null;

// DOM Elements
const loginScreen = document.getElementById('login-screen');
const dashboardScreen = document.getElementById('dashboard-screen');
const loginForm = document.getElementById('login-form');
const withdrawForm = document.getElementById('withdraw-form');
const depositForm = document.getElementById('deposit-form');
const alertBanner = document.getElementById('alert-banner');

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
    // Check if session exists in sessionStorage
    const savedUser = sessionStorage.getItem('atm_user');
    if (savedUser) {
        try {
            currentUser = JSON.parse(savedUser);
            loadDashboard();
        } catch (e) {
            sessionStorage.removeItem('atm_user');
        }
    }

    // Attach form submit listeners
    if (loginForm) loginForm.addEventListener('submit', handleLogin);
    if (withdrawForm) withdrawForm.addEventListener('submit', handleWithdraw);
    if (depositForm) depositForm.addEventListener('submit', handleDeposit);
});

// ========================================================
// 1. LOGIN HANDLER
// ========================================================
async function handleLogin(event) {
    event.preventDefault();
    hideAlert();

    const accountNumber = document.getElementById('account-number').value.trim();
    const pin = document.getElementById('pin').value.trim();

    if (!accountNumber || !pin) {
        showAlert('Please enter both Account Number and PIN.', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ accountNumber, pin })
        });

        const data = await response.json();

        if (data.success) {
            currentUser = {
                accountNumber: data.accountNumber,
                name: data.name,
                balance: data.balance
            };
            sessionStorage.setItem('atm_user', JSON.stringify(currentUser));
            document.getElementById('login-form').reset();
            showAlert('Login successful! Welcome.', 'success');
            setTimeout(() => {
                hideAlert();
                loadDashboard();
            }, 800);
        } else {
            showAlert(data.message || 'Login failed.', 'error');
        }
    } catch (error) {
        console.error('Login error:', error);
        showAlert('Unable to connect to Java ATM Server. Please ensure the backend is running.', 'error');
    }
}

// ========================================================
// 2. DASHBOARD & BALANCE MANAGEMENT
// ========================================================
async function loadDashboard() {
    if (!currentUser) return;

    loginScreen.classList.add('hidden');
    dashboardScreen.classList.remove('hidden');

    document.getElementById('welcome-message').textContent = `Welcome, ${currentUser.name}`;
    document.getElementById('user-acc-num').textContent = currentUser.accountNumber;

    await refreshBalance();
}

async function refreshBalance() {
    if (!currentUser) return;

    try {
        const response = await fetch(`${API_BASE}/balance?accountNumber=${encodeURIComponent(currentUser.accountNumber)}`);
        const data = await response.json();

        if (data.success) {
            currentUser.balance = data.balance;
            sessionStorage.setItem('atm_user', JSON.stringify(currentUser));

            const formattedBalance = formatCurrency(data.balance);
            document.getElementById('dashboard-balance').textContent = formattedBalance;
            document.getElementById('view-balance-amount').textContent = formattedBalance;
        }
    } catch (error) {
        console.error('Balance fetch error:', error);
    }
}

// ========================================================
// 3. WITHDRAWAL HANDLER
// ========================================================
async function handleWithdraw(event) {
    event.preventDefault();
    hideAlert();

    const amountInput = document.getElementById('withdraw-amount');
    const amount = parseFloat(amountInput.value);

    if (isNaN(amount) || amount <= 0) {
        showAlert('Please enter a valid amount greater than zero.', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/withdraw`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                accountNumber: currentUser.accountNumber,
                amount: amount
            })
        });

        const data = await response.json();

        if (data.success) {
            showAlert(`✅ Withdrawal Successful! Withdrawn: ${formatCurrency(data.withdrawnAmount)}. Remaining Balance: ${formatCurrency(data.remainingBalance)}`, 'success');
            amountInput.value = '';
            await refreshBalance();
        } else {
            showAlert(data.message || 'Withdrawal failed.', 'error');
        }
    } catch (error) {
        console.error('Withdrawal error:', error);
        showAlert('Error processing withdrawal transaction.', 'error');
    }
}

// ========================================================
// 4. DEPOSIT HANDLER
// ========================================================
async function handleDeposit(event) {
    event.preventDefault();
    hideAlert();

    const amountInput = document.getElementById('deposit-amount');
    const amount = parseFloat(amountInput.value);

    if (isNaN(amount) || amount <= 0) {
        showAlert('Please enter a valid amount greater than zero.', 'error');
        return;
    }

    try {
        const response = await fetch(`${API_BASE}/deposit`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                accountNumber: currentUser.accountNumber,
                amount: amount
            })
        });

        const data = await response.json();

        if (data.success) {
            showAlert(`✅ Deposit Successful! Deposited: ${formatCurrency(data.depositedAmount)}. Updated Balance: ${formatCurrency(data.updatedBalance)}`, 'success');
            amountInput.value = '';
            await refreshBalance();
        } else {
            showAlert(data.message || 'Deposit failed.', 'error');
        }
    } catch (error) {
        console.error('Deposit error:', error);
        showAlert('Error processing deposit transaction.', 'error');
    }
}

// ========================================================
// 5. MINI STATEMENT HANDLER
// ========================================================
async function loadMiniStatement() {
    if (!currentUser) return;

    const tbody = document.getElementById('statement-body');
    tbody.innerHTML = '<tr><td colspan="4" class="text-center">Loading transactions...</td></tr>';

    try {
        const response = await fetch(`${API_BASE}/ministatement?accountNumber=${encodeURIComponent(currentUser.accountNumber)}`);
        const data = await response.json();

        if (data.success && data.transactions) {
            if (data.transactions.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4" class="text-center">No transaction history available.</td></tr>';
                return;
            }

            tbody.innerHTML = data.transactions.map(tx => {
                const isDeposit = tx.type === 'DEPOSIT';
                const typeClass = isDeposit ? 'badge-deposit' : 'badge-withdraw';
                return `
                    <tr>
                        <td>${tx.date}</td>
                        <td><span class="${typeClass}">${tx.type}</span></td>
                        <td>${formatCurrency(tx.amount)}</td>
                        <td>${formatCurrency(tx.balanceAfter)}</td>
                    </tr>
                `;
            }).join('');

        } else {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center">Unable to load transactions.</td></tr>';
        }
    } catch (error) {
        console.error('Mini statement error:', error);
        tbody.innerHTML = '<tr><td colspan="4" class="text-center">Error fetching mini statement.</td></tr>';
    }
}

// ========================================================
// 6. VIEW SWITCHING UTILITIES
// ========================================================
function showView(viewId) {
    hideAlert();
    hideViews();

    const view = document.getElementById(viewId);
    if (view) {
        view.classList.remove('hidden');
        if (viewId === 'balance-view') {
            refreshBalance();
        } else if (viewId === 'statement-view') {
            loadMiniStatement();
        }
    }
}

function hideViews() {
    const views = document.querySelectorAll('.action-view');
    views.forEach(view => view.classList.add('hidden'));
}

// ========================================================
// 7. LOGOUT & ALERT HELPERS
// ========================================================
function logout() {
    currentUser = null;
    sessionStorage.removeItem('atm_user');

    hideViews();
    dashboardScreen.classList.add('hidden');
    loginScreen.classList.remove('hidden');
    document.getElementById('login-form').reset();

    showAlert('Logged out successfully.', 'info');
    setTimeout(hideAlert, 2000);
}

function showAlert(message, type = 'info') {
    alertBanner.textContent = message;
    alertBanner.className = `alert-banner ${type}`;
    alertBanner.classList.remove('hidden');
}

function hideAlert() {
    alertBanner.classList.add('hidden');
}

function formatCurrency(amount) {
    return '₹' + Number(amount).toLocaleString('en-IN', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });
}
