# SendGrid Email Integration

## Overview

The application uses SendGrid for sending password reset emails with dynamic template data. The `ResetPasswordService` integrates with `SendGridService` to send personalized reset password emails.

## Dynamic Fields Used in SendGrid Templates

The following dynamic fields are populated and sent to SendGrid templates for password reset emails:

| Field Name | Description | Source |
|------------|-------------|---------|
| `accountName` | Name of the e-commerce account | `EcomAccount.getName()` (fallback: "Radyfy") |
| `resetLink` | Complete password reset URL | Constructed from account domain + request ID + email |
| `logoUrl` | Account logo URL | `EcomAccount.getLogo()` (fallback: empty string) |
| `currentYear` | Current year for copyright | `java.time.Year.now().getValue()` |

## Configuration Properties

The following properties need to be configured in your `application.properties` file:

```properties
# SendGrid Configuration
app.sendgrid.api_key=your_sendgrid_api_key_here
app.sendgrid.reset_password_template_id=your_template_id_here
app.admin.fromEmail=noreply@yourdomain.com
```

## Files Involved

- `src/main/java/com/radyfy/common/service/utils/ResetPasswordService.java` - Main service handling password reset logic
- `src/main/java/com/radyfy/common/service/email/SendGridService.java` - SendGrid integration service

## Email Flow

1. User requests password reset via `sendForgetEmail(String toEmail)`
2. System creates reset request document with unique request ID
3. Dynamic data is prepared with account information and reset link
4. SendGrid service sends email using template with dynamic data
5. User clicks reset link and submits new password via `verifyAndResetPassword()`

## SendGrid Service Implementation

The `SendGridService` class provides the core functionality for sending emails:

```java
public Response sendEmail(String fromEmail, String toEmail, String templateId, Map<String, Object> dynamicData)
```

### Parameters:
- `fromEmail` - Sender email address
- `toEmail` - Recipient email address  
- `templateId` - SendGrid template ID
- `dynamicData` - Map of dynamic template variables

### Features:
- Uses SendGrid's dynamic template system
- Supports personalized email content
- Includes error handling and logging
- Returns SendGrid response for monitoring

## Template Setup

To use this integration, you need to:

1. Create a SendGrid account and obtain an API key
2. Create a dynamic template in SendGrid dashboard
3. Configure the template with placeholders for the dynamic fields:
   - `{{accountName}}`
   - `{{resetLink}}`
   - `{{logoUrl}}`
   - `{{currentYear}}`
4. Update your application properties with the template ID

## Error Handling

The service includes comprehensive error handling:
- IOException handling for network issues
- Runtime exceptions for failed email sends
- Detailed logging of response status and headers
