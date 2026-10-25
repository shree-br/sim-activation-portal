"use strict";

// ========================================
// SIM ACTIVATION PORTAL
// ========================================

const API_URL = "/api/customers";

let editingCustomerId = null;

const $ = (id) => document.getElementById(id);

// ========================================
// MESSAGES
// ========================================

function showMessage(text, isError = false) {
    const box = $("message");
    box.textContent = text;
    box.style.color = isError ? "#dc3545" : "#198754";
}

function showSearchMessage(text, isError = false) {
    const box = $("searchMessage");
    box.textContent = text;
    box.style.color = isError ? "#dc3545" : "#198754";
}

// ========================================
// API HELPER
// ========================================

async function apiRequest(url, options = {}) {
    const response = await fetch(url, options);

    if (!response.ok) {
        const text = await response.text();
        throw new Error(
            text || `Request failed with status ${response.status}`
        );
    }

    if (response.status === 204) {
        return null;
    }

    const contentType = response.headers.get("content-type") || "";

    if (contentType.includes("application/json")) {
        return response.json();
    }

    return null;
}

// ========================================
// WELCOME PAGE
// ========================================

$("startButton").addEventListener("click", () => {
    $("welcomePage").hidden = true;
    $("activationPage").hidden = false;

    window.scrollTo({
        top: 0,
        behavior: "smooth"
    });
});

// ========================================
// FORM HELPERS
// ========================================

function getCustomerFormData() {
    const customer = {
        firstName: $("firstName").value.trim(),
        lastName: $("lastName").value.trim(),
        email: $("email").value.trim(),
        dateOfBirth: $("dob").value,
        mobile: $("mobile").value.trim(),
        simNumber: $("simNumber").value.trim(),
        address: $("address").value.trim(),
        idProof: $("idProof").value
    };

    const aadhaar = $("aadhaar").value.trim();

    if (aadhaar) {
        customer.aadhaar = aadhaar;
    }

    return customer;
}

function resetForm() {
    $("simForm").reset();

    editingCustomerId = null;

    $("customerId").value = "";
    $("aadhaar").required = true;

    $("formTitle").textContent = "Register New SIM";
    $("submitButton").textContent = "Register SIM";
    $("cancelEditButton").hidden = true;
    $("referenceResult").hidden = true;

    showMessage("");
}

// ========================================
// APPLICATION REFERENCE NUMBER
// ========================================

function displayReferenceNumber(reference) {
    const referenceBox = $("referenceResult");
    const referenceText = $("referenceNumber");

    if (!reference) {
        referenceBox.hidden = true;
        showMessage(
            "Application submitted, but no reference number was returned. Please contact the administrator.",
            true
        );
        return;
    }

    referenceText.textContent = reference;
    referenceBox.hidden = false;

    showMessage("Application registered successfully!");

    referenceBox.scrollIntoView({
        behavior: "smooth",
        block: "center"
    });
}

// Copy reference number
$("copyReferenceButton").addEventListener("click", async () => {
    const reference = $("referenceNumber").textContent;

    if (!reference || reference === "-") {
        showMessage("No reference number available.", true);
        return;
    }

    try {
        await navigator.clipboard.writeText(reference);
        showMessage("Reference number copied: " + reference);
    } catch (error) {
        // Fallback for browsers where clipboard access is unavailable.
        const temporaryInput = document.createElement("textarea");
        temporaryInput.value = reference;
        temporaryInput.style.position = "fixed";
        temporaryInput.style.opacity = "0";

        document.body.appendChild(temporaryInput);
        temporaryInput.select();

        const copied = document.execCommand("copy");
        temporaryInput.remove();

        if (copied) {
            showMessage("Reference number copied: " + reference);
        } else {
            showMessage(
                "Please copy this reference number manually: " + reference,
                true
            );
        }
    }
});

// ========================================
// REGISTER AND UPDATE CUSTOMER
// ========================================

$("simForm").addEventListener("submit", async (event) => {
    event.preventDefault();

    const isEditing = editingCustomerId !== null;

    const url = isEditing
        ? `${API_URL}/${editingCustomerId}`
        : API_URL;

    const method = isEditing ? "PUT" : "POST";

    $("submitButton").disabled = true;

    // Hide any previous reference number.
    $("referenceResult").hidden = true;

    try {
        showMessage(
            isEditing
                ? "Updating customer..."
                : "Registering customer..."
        );

        const customer = getCustomerFormData();

        const savedCustomer = await apiRequest(url, {
            method,
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(customer)
        });

        if (isEditing) {
            showMessage("Customer updated successfully!");
            resetForm();
        } else {
            // The backend must return applicationReference in its JSON response.
            const reference = savedCustomer
                ? savedCustomer.applicationReference
                : null;

            $("simForm").reset();
            $("customerId").value = "";
            $("aadhaar").required = true;

            displayReferenceNumber(reference);
        }

        await loadCustomers();

    } catch (error) {
        console.error("Save error:", error);
        showMessage(error.message || "Unable to save customer.", true);

    } finally {
        $("submitButton").disabled = false;
    }
});

// ========================================
// CANCEL EDIT
// ========================================

$("cancelEditButton").addEventListener("click", resetForm);

// ========================================
// DASHBOARD STATISTICS
// ========================================

function updateDashboard(customers) {
    $("totalCustomers").textContent = customers.length;

    $("pendingCustomers").textContent =
        customers.filter(c =>
            !c.activationStatus ||
            c.activationStatus === "PENDING"
        ).length;

    $("approvedCustomers").textContent =
        customers.filter(c =>
            c.activationStatus === "APPROVED"
        ).length;

    $("rejectedCustomers").textContent =
        customers.filter(c =>
            c.activationStatus === "REJECTED"
        ).length;
}

// ========================================
// LOAD CUSTOMERS
// ========================================

async function loadCustomers() {
    const customerList = $("customerList");
    customerList.textContent = "Loading customers...";

    try {
        const customers = await apiRequest(API_URL);

        if (!Array.isArray(customers)) {
            throw new Error("Unexpected customer data received.");
        }

        updateDashboard(customers);
        renderCustomers(customers);

    } catch (error) {
        console.error("Load customers error:", error);

        customerList.textContent =
            "Unable to load customers: " + error.message;
    }
}

// ========================================
// CUSTOMER TABLE
// ========================================

function createCell(value) {
    const td = document.createElement("td");
    td.textContent = value ?? "";
    return td;
}

function createButton(label, className, callback, disabled = false) {
    const button = document.createElement("button");

    button.type = "button";
    button.textContent = label;
    button.className = className;
    button.disabled = disabled;

    button.addEventListener("click", callback);

    return button;
}

function renderCustomers(customers) {
    const customerList = $("customerList");

    if (customers.length === 0) {
        customerList.textContent = "No customers registered yet.";
        return;
    }

    const table = document.createElement("table");
    const thead = document.createElement("thead");
    const headerRow = document.createElement("tr");

    const headers = [
        "ID",
        "Application Reference",
        "First Name",
        "Last Name",
        "Email",
        "Mobile",
        "SIM Number",
        "ID Proof",
        "Activation Status",
        "Actions"
    ];

    headers.forEach(header => {
        const th = document.createElement("th");
        th.textContent = header;
        headerRow.appendChild(th);
    });

    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement("tbody");

    customers.forEach(customer => {
        const row = document.createElement("tr");

        [
            customer.id,
            customer.applicationReference || "-",
            customer.firstName,
            customer.lastName,
            customer.email,
            customer.mobile,
            customer.simNumber,
            customer.idProof
        ].forEach(value => {
            row.appendChild(createCell(value));
        });

        const status = customer.activationStatus || "PENDING";
        const statusCell = createCell(status);

        statusCell.className = "status-" + status.toLowerCase();
        row.appendChild(statusCell);

        const actionsCell = document.createElement("td");
        actionsCell.className = "actions-cell";

        actionsCell.appendChild(
            createButton(
                "Approve",
                "approve-button",
                () => updateActivationStatus(customer.id, "APPROVED"),
                status === "APPROVED"
            )
        );

        actionsCell.appendChild(
            createButton(
                "Reject",
                "reject-button",
                () => updateActivationStatus(customer.id, "REJECTED"),
                status === "REJECTED"
            )
        );

        actionsCell.appendChild(
            createButton(
                "Edit",
                "edit-button",
                () => editCustomer(customer.id)
            )
        );

        actionsCell.appendChild(
            createButton(
                "Delete",
                "delete-button",
                () => deleteCustomer(customer.id)
            )
        );

        row.appendChild(actionsCell);
        tbody.appendChild(row);
    });

    table.appendChild(tbody);

    const wrapper = document.createElement("div");
    wrapper.className = "table-wrapper";
    wrapper.appendChild(table);

    customerList.replaceChildren(wrapper);
}

// ========================================
// SEARCH CUSTOMER BY MOBILE
// ========================================

$("searchButton").addEventListener("click", searchCustomer);

$("searchMobile").addEventListener("keydown", event => {
    if (event.key === "Enter") {
        event.preventDefault();
        searchCustomer();
    }
});

async function searchCustomer() {
    const mobile = $("searchMobile").value.trim();

    $("searchResult").replaceChildren();
    showSearchMessage("");

    if (!/^[0-9]{10}$/.test(mobile)) {
        showSearchMessage(
            "Enter a valid 10-digit mobile number.",
            true
        );
        return;
    }

    $("searchButton").disabled = true;

    try {
        const customer = await apiRequest(
            `${API_URL}/search/mobile/${encodeURIComponent(mobile)}`
        );

        if (!customer) {
            showSearchMessage("Customer not found.", true);
            return;
        }

        renderSearchResult(customer);
        showSearchMessage("Customer found successfully.");

    } catch (error) {
        console.error("Search error:", error);
        showSearchMessage("Customer not found or search failed.", true);

    } finally {
        $("searchButton").disabled = false;
    }
}

// ========================================
// DISPLAY SEARCH RESULT
// ========================================

function renderSearchResult(customer) {
    const table = document.createElement("table");
    const tbody = document.createElement("tbody");

    const details = [
        ["Customer ID", customer.id],
        ["Application Reference", customer.applicationReference],
        ["First Name", customer.firstName],
        ["Last Name", customer.lastName],
        ["Email", customer.email],
        ["Date of Birth", customer.dateOfBirth],
        ["Mobile", customer.mobile],
        ["SIM Number", customer.simNumber],
        ["Address", customer.address],
        ["ID Proof", customer.idProof],
        ["Activation Status", customer.activationStatus || "PENDING"]
    ];

    details.forEach(([label, value]) => {
        const row = document.createElement("tr");

        const heading = document.createElement("th");
        heading.textContent = label;

        row.appendChild(heading);
        row.appendChild(createCell(value));

        tbody.appendChild(row);
    });

    table.appendChild(tbody);
    $("searchResult").replaceChildren(table);
}

$("clearSearchButton").addEventListener("click", () => {
    $("searchMobile").value = "";
    $("searchResult").replaceChildren();
    showSearchMessage("");
});

// ========================================
// EDIT CUSTOMER
// ========================================

async function editCustomer(id) {
    try {
        // Fixed URL interpolation.
        const customer = await apiRequest(`${API_URL}/${id}`);

        $("customerId").value = customer.id;

        $("firstName").value = customer.firstName ?? "";
        $("lastName").value = customer.lastName ?? "";
        $("email").value = customer.email ?? "";
        $("dob").value = customer.dateOfBirth ?? "";
        $("mobile").value = customer.mobile ?? "";
        $("simNumber").value = customer.simNumber ?? "";
        $("address").value = customer.address ?? "";
        $("idProof").value = customer.idProof ?? "";

        // Never display the stored Aadhaar value.
        $("aadhaar").value = "";
        $("aadhaar").required = false;

        editingCustomerId = id;

        $("formTitle").textContent = "Update Customer";
        $("submitButton").textContent = "Update Customer";
        $("cancelEditButton").hidden = false;
        $("referenceResult").hidden = true;

        $("activationPage").hidden = false;
        $("welcomePage").hidden = true;

        showMessage("Editing customer ID: " + id);

        window.scrollTo({
            top: 0,
            behavior: "smooth"
        });

    } catch (error) {
        console.error("Edit error:", error);
        alert("Unable to load customer: " + error.message);
    }
}

// ========================================
// DELETE CUSTOMER
// ========================================

async function deleteCustomer(id) {
    const confirmed = confirm(
        `Are you sure you want to delete customer ID ${id}?`
    );

    if (!confirmed) {
        return;
    }

    try {
        await apiRequest(`${API_URL}/${id}`, {
            method: "DELETE"
        });

        if (editingCustomerId === id) {
            resetForm();
        }

        $("searchResult").replaceChildren();
        showSearchMessage("");

        await loadCustomers();

        alert("Customer deleted successfully!");

    } catch (error) {
        console.error("Delete error:", error);
        alert("Unable to delete customer: " + error.message);
    }
}

// ========================================
// APPROVE / REJECT ACTIVATION
// ========================================

async function updateActivationStatus(id, status) {
    const confirmed = confirm(
        `Change customer ID ${id} status to ${status}?`
    );

    if (!confirmed) {
        return;
    }

    try {
        await apiRequest(`${API_URL}/${id}/status`, {
            method: "PUT",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                activationStatus: status
            })
        });

        alert("Activation status updated to " + status + "!");

        await loadCustomers();

    } catch (error) {
        console.error("Status update error:", error);

        alert(
            "Unable to update status. Check your admin login and backend endpoint. " +
            error.message
        );
    }
}

// ========================================
// REFRESH CUSTOMERS
// ========================================

$("viewCustomers").addEventListener("click", loadCustomers);

// ========================================
// INITIALIZE PORTAL
// ========================================

document.addEventListener("DOMContentLoaded", () => {
    loadCustomers();
});