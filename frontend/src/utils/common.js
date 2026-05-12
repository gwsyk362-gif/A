import { marked } from 'marked';
import { markedHighlight } from 'marked-highlight';

let markedConfigured = false;

export function setupMarked() {
  if (markedConfigured) return;
  marked.use(
    markedHighlight({
      highlight(code, lang) {
        if (lang) {
          return `<pre><code class="language-${lang}">${escapeHtml(code)}</code></pre>`;
        }
        return `<pre><code>${escapeHtml(code)}</code></pre>`;
      },
    })
  );
  markedConfigured = true;
}

export function escapeHtml(html) {
  return String(html)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}

export function getCurrentUserId() {
  const savedUser = localStorage.getItem('currentUser');
  if (!savedUser) return null;
  try {
    return JSON.parse(savedUser).userId;
  } catch {
    return null;
  }
}

export function formatDate(dateStr) {
  if (!dateStr) return '';
  return new Date(dateStr).toLocaleDateString();
}

export function formatDateTime(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return `${d.toLocaleDateString()} ${d.getHours()}:${String(d.getMinutes()).padStart(2, '0')}`;
}

export function formatSeconds(sec) {
  const m = Math.floor(sec / 60);
  const s = sec % 60;
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
}

export function formatOptions(str) {
  if (!str) return [];
  const regex = /[A-D][\.．、\s][\s\S]*?(?=[A-D][\.．、\s]|$)/g;
  return (str.match(regex) || []).map(o => o.trim());
}

export function getOptionLetter(opt) {
  return opt.trim().charAt(0).toUpperCase();
}

export function renderContent(article) {
  if (!article?.content) return '';
  if (article.contentType === 'markdown') {
    return marked.parse(article.content);
  }
  return article.content;
}

export function isMp4(url) {
  return url && (url.toLowerCase().endsWith('.mp4') || url.toLowerCase().endsWith('.webm'));
}

// Auto-configure marked on first import
setupMarked();