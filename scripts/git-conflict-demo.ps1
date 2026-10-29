# Task 6: creates a REAL merge conflict between two feature branches, so you can resolve it and screenshot it.
# Run from the project root on an up-to-date 'develop' branch with a clean working tree.
$ErrorActionPreference = "Stop"
git checkout develop
git checkout -b feature/footer-text-a
(Get-Content src/main/resources/static/index.html -Raw) -replace 'Theses, papers and reports, reviewed before they are published\.', 'Theses, papers and reports from our departments.' | Set-Content src/main/resources/static/index.html -NoNewline
git commit -am "docs(ui): change header tagline (version A)"
git checkout develop
git checkout -b feature/footer-text-b
(Get-Content src/main/resources/static/index.html -Raw) -replace 'Theses, papers and reports, reviewed before they are published\.', 'The university’s open archive of reviewed research.' | Set-Content src/main/resources/static/index.html -NoNewline
git commit -am "docs(ui): change header tagline (version B)"
git checkout develop
git merge --no-ff feature/footer-text-a -m "Merge feature/footer-text-a"
Write-Host "Now merging B: a conflict is expected." -ForegroundColor Yellow
git merge feature/footer-text-b
Write-Host "Resolve the markers in src/main/resources/static/index.html, then: git add . ; git commit" -ForegroundColor Yellow
