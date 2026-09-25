# Privacy Policy for Pile

**Last updated:** September 25, 2026

Rubén Alfonso ("the Developer") built the **Pile** application as a free app. This service is provided at no cost and is intended for use as is.

This page is used to inform visitors regarding the policies regarding the collection, use, and disclosure of personal information when using this service.


## 1. Core Principle: Local-First and Privacy-First

Pile is designed with a **local-first** approach.

By default, all data created or managed within the application — including imported documents, images, tags ("piles"), drafts, and database entries — is stored **exclusively on your device's internal storage**. 

The Developer operates no servers, databases, or cloud infrastructure. Pile does not collect, sell, monetize, or transmit your personal documents to the Developer or any unauthorized third party.


## 2. Optional Cloud Backup (Google Drive)

Pile offers an **optional** cloud backup and restore feature using Google Drive. This feature is disabled by default and requires explicit activation by the user.

### How Google Drive Integration Works
* **Direct Transfer:** When you enable Google Drive backup, data is transferred directly between your device and your personal Google Drive storage. No data ever passes through or resides on servers owned or operated by the Developer.
* **Minimal Access (`drive.file` Scope):** Pile requests strictly scoped access (`https://www.googleapis.com/auth/drive.file`). This technical restriction ensures the application can **only** access, create, and modify files and folders that Pile itself created. Pile cannot view, read, modify, or delete any other files, photos, or documents in your Google Drive.
* **Account Information:** To authenticate with Google Drive, the app uses standard Google Sign-In / Credential Manager services. The user's email address and authentication tokens are processed solely on your device to maintain session state and display connected account information. They are never sent to external servers or logged by the Developer.
* **Optional End-to-End Encryption (E2EE):** When encryption is enabled for backups, your data is encrypted locally using AES-GCM before transmission. The Developer does not hold or store encryption keys; you are solely responsible for retaining your recovery key.


## 3. Third-Party Services and Analytics

Pile does **not** integrate third-party tracking or advertising services:
* No analytics SDKs (e.g., Firebase Analytics, Google Analytics)
* No advertising networks
* No third-party crash reporting tools

The only third-party network interaction occurs if you explicitly connect your personal Google Drive account for backups.


## 4. Permissions

Pile requests only the minimum device permissions necessary to perform its features:
* **Storage / Media Access (or system file picker):** Used strictly to import and export your documents, PDFs, and images locally.
* **Internet Access:** Used exclusively to interact with the official Google Drive API when you perform backup or restore operations.


## 5. Data Retention and Deletion

* **Local Data:** All local data remains on your device until you delete individual items within the app or uninstall the application.
* **Google Drive Data:** Backups stored in your Google Drive remain under your control. You can delete them directly through Google Drive or disconnect Pile's access at any time via your [Google Account Security Settings](https://myaccount.google.com/permissions).


## 6. Children's Privacy

Pile does not knowingly collect or solicit personal information from anyone, including children under the age of 13. In compliance with Google's API policies, Google Sign-In features within this app are not directed to children under 13.


## Changes to This Privacy Policy

This Privacy Policy may be updated from time to time. Any changes will be reflected on this page with an updated revision date.

## Contact

If you have any questions or suggestions about this Privacy Policy, do not hesitate to contact the Developer at:

**Email:** rubenalfonso.dev@gmail.com

---

This Privacy Policy is effective as of September 25, 2026.
