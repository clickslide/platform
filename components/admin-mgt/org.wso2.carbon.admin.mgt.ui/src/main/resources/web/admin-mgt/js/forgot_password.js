function initiatePasswordReset() {
    var resetPasswordForm = document.getElementById('resetPasswordForm');
    resetPasswordForm.submit();
}

function getDomainFromUserName() {
    var tenantDomainForm = document.getElementById("domain");
    var tenantDomain = tenantDomainForm.value;
    var userName = document.getElementById("username").value;
    var atIndex = userName.lastIndexOf('@');
    if (userName != null) {
        if (atIndex == -1) {                                        
            tenantDomain = "";
        } else {
            tenantDomain = userName.substring(atIndex + 1, userName.length);
        }
    }
    tenantDomainForm.value = tenantDomain;
}

function getTenantAwareUserName() {
    var userName = document.getElementById("username").value;
    var adminForm = document.getElementById("admin");
    var admin = adminForm.value;
    var atIndex = userName.lastIndexOf('@');
    if (userName != null) {
    admin = userName;
     if (atIndex != -1) {
        admin = userName.substring(0, atIndex);
    }
    }
    adminForm.value = admin;
}
