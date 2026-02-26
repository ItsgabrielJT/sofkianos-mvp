#!/usr/bin/env node
/**
 * Script: create-eslint-issues.js
 * Purpose: Automatically creates GitHub issues from ESLint or SpotBugs report
 * Usage: Called by GitHub Actions workflow after lint execution
 * 
 * Environment Variables Required:
 * - GITHUB_TOKEN: GitHub Actions token for API access
 * - GITHUB_REPOSITORY: Repository in format "owner/repo"
 * - GITHUB_SHA: Full commit SHA
 * - GITHUB_REF_NAME: Branch name
 * - GITHUB_ACTOR: User who triggered the workflow
 * - GITHUB_RUN_ID: Workflow run ID for logs link
 */

const fs = require('fs');
const path = require('path');
const { Octokit } = require('@octokit/rest');
let xml2js;
try {
  xml2js = require('xml2js');
} catch (e) {
  // Will fail if not needed
}

// Initialize Octokit with GitHub token
const octokit = new Octokit({ 
  auth: process.env.GITHUB_TOKEN 
});

// Extract repository owner and name
const [owner, repo] = process.env.GITHUB_REPOSITORY.split('/');
const commitHash = process.env.GITHUB_SHA?.substring(0, 7) || 'unknown';
const fullCommitHash = process.env.GITHUB_SHA || 'unknown';
const commitAuthor = process.env.GITHUB_ACTOR || 'unknown';
const branchName = process.env.GITHUB_REF_NAME || 'unknown';
const runId = process.env.GITHUB_RUN_ID || '';

/**
 * Main function to process ESLint or SpotBugs report and create issues
 */
async function createIssuesFromLint() {
  // Try ESLint report first (frontend)
  const eslintReportPath = path.join(process.cwd(), 'frontend', 'eslint-report.json');
  // Try SpotBugs reports (backend)
  const spotbugsProducerPath = path.join(process.cwd(), 'producer-api', 'target', 'spotbugsXml.xml');
  const spotbugsConsumerPath = path.join(process.cwd(), 'consumer-worker', 'target', 'spotbugsXml.xml');

  if (fs.existsSync(eslintReportPath)) {
    await createIssuesFromEslint(eslintReportPath);
    return;
  }

  // Collect all SpotBugs issues from both backend modules
  const spotbugsFiles = [
    { path: spotbugsProducerPath, module: 'producer-api' },
    { path: spotbugsConsumerPath, module: 'consumer-worker' }
  ];
  let allIssues = [];
  for (const file of spotbugsFiles) {
    if (fs.existsSync(file.path)) {
      const issues = await parseSpotBugsXml(file.path, file.module);
      allIssues = allIssues.concat(issues);
    }
  }

  if (allIssues.length === 0) {
    console.log('✅ No linting issues found (ESLint or SpotBugs). Great job!');
    return;
  }

  // Get existing open issues to avoid duplicates
  console.log('🔎 Checking for existing issues...');
  const existingIssues = await getExistingLintIssues();

  // Create issues for each SpotBugs error
  let createdCount = 0;
  let skippedCount = 0;
  for (const issue of allIssues) {
    const isDuplicate = await isIssueDuplicate(issue, existingIssues);
    if (isDuplicate) {
      console.log(`⏭️  Skipping duplicate: ${issue.message.ruleId || issue.message.type || 'spotbugs'} in ${getRelativePath(issue.file)}`);
      skippedCount++;
      continue;
    }
    await createGitHubIssue(issue);
    createdCount++;
    await sleep(1000);
  }
  console.log('\n📈 Summary:');
  console.log(`   ✅ Created: ${createdCount} issues`);
  console.log(`   ⏭️  Skipped: ${skippedCount} duplicates`);
  console.log(`   📊 Total: ${allIssues.length} linting violations`);
}

// ESLint logic (unchanged, but now parameterized)
async function createIssuesFromEslint(reportPath) {
  console.log('🔍 Reading ESLint report...');
  const report = JSON.parse(fs.readFileSync(reportPath, 'utf8'));
  const issues = [];
  let totalErrors = 0;
  let totalWarnings = 0;
  report.forEach(file => {
    if (file.errorCount === 0 && file.warningCount === 0) return;
    totalErrors += file.errorCount;
    totalWarnings += file.warningCount;
    file.messages.forEach(msg => {
      issues.push({
        file: file.filePath,
        message: msg
      });
    });
  });
  console.log(`📊 Found ${totalErrors} errors and ${totalWarnings} warnings`);
  if (issues.length === 0) {
    console.log('✅ No linting issues found. Great job!');
    return;
  }
  const existingIssues = await getExistingLintIssues();
  let createdCount = 0;
  let skippedCount = 0;
  for (const issue of issues) {
    const isDuplicate = await isIssueDuplicate(issue, existingIssues);
    if (isDuplicate) {
      console.log(`⏭️  Skipping duplicate: ${issue.message.ruleId || 'syntax-error'} in ${getRelativePath(issue.file)}`);
      skippedCount++;
      continue;
    }
    await createGitHubIssue(issue);
    createdCount++;
    await sleep(1000);
  }
  console.log('\n📈 Summary:');
  console.log(`   ✅ Created: ${createdCount} issues`);
  console.log(`   ⏭️  Skipped: ${skippedCount} duplicates`);
  console.log(`   📊 Total: ${issues.length} linting violations`);
}

// SpotBugs XML parsing
async function parseSpotBugsXml(xmlPath, moduleName) {
  if (!xml2js) {
    console.error('❌ xml2js is required for SpotBugs XML parsing. Please install it with "npm install xml2js".');
    return [];
  }
  const xml = fs.readFileSync(xmlPath, 'utf8');
  let issues = [];
  await xml2js.parseStringPromise(xml, { explicitArray: false })
    .then(result => {
      const bugInstances = result.BugCollection?.BugInstance || [];
      const bugs = Array.isArray(bugInstances) ? bugInstances : [bugInstances];
      for (const bug of bugs) {
        // SpotBugs can have multiple SourceLine entries, pick the first
        const source = bug.SourceLine && (Array.isArray(bug.SourceLine) ? bug.SourceLine[0] : bug.SourceLine);
        const file = source?.sourcepath ? path.join(moduleName, 'src', source.sourcepath) : moduleName;
        const line = source?.start ? parseInt(source.start, 10) : 1;
        issues.push({
          file,
          message: {
            ruleId: bug.type,
            severity: bug.priority == 1 ? 2 : 1, // 1=High, 2=Normal, 3=Low
            message: bug.LongMessage || bug.ShortMessage || bug.type,
            line,
            type: bug.type,
            category: bug.category
          }
        });
      }
    })
    .catch(err => {
      console.error(`❌ Failed to parse SpotBugs XML: ${err.message}`);
    });
  return issues;
}

/**
 * Gets all existing open issues with 'eslint' label
 */
async function getExistingLintIssues() {
  try {
    const { data } = await octokit.issues.listForRepo({
      owner,
      repo,
      state: 'open',
      labels: 'eslint',
      per_page: 100
    });
    return data;
  } catch (error) {
    console.error(`⚠️  Error fetching existing issues: ${error.message}`);
    return [];
  }
}

/**
 * Checks if an issue already exists
 */
async function isIssueDuplicate(issue, existingIssues) {
  const expectedTitle = generateIssueTitle(issue);
  return existingIssues.some(existing => existing.title === expectedTitle);
}

/**
 * Generates the issue title
 */
function generateIssueTitle(issue) {
  const errorType = issue.message.ruleId || issue.message.type || 'lint-error';
  const relativePath = getRelativePath(issue.file);
  return `[LINT] ${errorType} in ${relativePath}`;
}

/**
 * Generates the issue body with the custom format
 */
function generateIssueBody(issue) {
  const msg = issue.message;
  const relativePath = getRelativePath(issue.file);
  const isSpotBugs = msg.type !== undefined && msg.category !== undefined;
  const severity = msg.severity === 2 ? 'Error' : 'Warning';
  const ruleId = msg.ruleId || msg.type || 'lint-error';
  const ruleLink = isSpotBugs
    ? `https://spotbugs.readthedocs.io/en/stable/bugDescriptions.html#${ruleId}`
    : (msg.ruleId ? `https://eslint.org/docs/latest/rules/${msg.ruleId}` : 'N/A');
  // Read source code context (3 lines before and after)
  const sourceContext = getSourceContext(issue.file, msg.line);
  const workflowLogsUrl = `https://github.com/${owner}/${repo}/actions/runs/${runId}`;
  return `## 🚨 ${isSpotBugs ? 'SpotBugs' : 'ESLint'} Violation Detected

**File:** \
${relativePath}:${msg.line}  
**Rule:** [${ruleId}](${ruleLink})  
**Severity:** ${severity}

### 📝 Error Message
> ${msg.message}

### 🛠 Automated Context
- **Commit:** [${commitHash}](https://github.com/${owner}/${repo}/commit/${fullCommitHash})
- **Author:** @${commitAuthor}
- **Branch:** ${branchName}
- **Workflow Run:** [View Logs](${workflowLogsUrl})

### 🔍 Code Context
\`\`\`${isSpotBugs ? 'java' : 'typescript'}
// ${relativePath}:${msg.line}
${sourceContext}
\`\`\`

---
_This issue was automatically generated by the ${isSpotBugs ? 'Backend' : 'Frontend'} CI/CD workflow. Please fix the linting error and close this issue._`;
}

/**
 * Gets relative path from workspace root
 */
function getRelativePath(filePath) {
  const workspaceRoot = process.cwd();
  return filePath.replace(workspaceRoot + '/', '').replace(/^\//, '');
}

/**
 * Reads source code context around the error line
 */
function getSourceContext(filePath, lineNumber) {
  try {
    const content = fs.readFileSync(filePath, 'utf8');
    const lines = content.split('\n');
    
    const contextBefore = 2;
    const contextAfter = 2;
    const startLine = Math.max(0, lineNumber - contextBefore - 1);
    const endLine = Math.min(lines.length, lineNumber + contextAfter);
    
    const contextLines = [];
    for (let i = startLine; i < endLine; i++) {
      const lineNum = i + 1;
      const marker = lineNum === lineNumber ? '→ ' : '  ';
      contextLines.push(`${marker}${lineNum.toString().padStart(4)} | ${lines[i]}`);
    }
    
    return contextLines.join('\n');
  } catch (error) {
    return `Error reading file: ${error.message}`;
  }
}

/**
 * Creates a GitHub issue
 */
async function createGitHubIssue(issue) {
  const title = generateIssueTitle(issue);
  const body = generateIssueBody(issue);
  const severity = issue.message.severity === 2 ? 'high' : 'medium';
  
  const labels = [
    'eslint',
    'code-quality',
    `priority-${severity}`,
    issue.message.severity === 2 ? 'bug' : 'technical-debt'
  ];
  
  try {
    await octokit.issues.create({
      owner,
      repo,
      title,
      body,
      labels
    });
    
    console.log(`✅ Created: ${title}`);
  } catch (error) {
    console.error(`❌ Failed to create issue "${title}": ${error.message}`);
  }
}

/**
 * Sleep utility for rate limiting
 */
function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

// Execute the script
createIssuesFromLint().catch(error => {
  console.error('❌ Fatal error:', error);
  process.exit(1);
});
