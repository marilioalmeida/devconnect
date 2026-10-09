export interface TextSegment {
  type: 'text';
  value: string;
}

export interface CodeSegment {
  type: 'code';
  language: string;
  value: string;
}

export type Segment = TextSegment | CodeSegment;

export interface Chunk {
  type: 'text' | 'inline-code' | 'link';
  value: string;
}

const FENCE = /```[ \t]*([^\n`]*)(?:\r?\n)?([\s\S]*?)(?:```|$)/g;
const INLINE_OR_LINK = /`([^`\n]+)`|(https?:\/\/[^\s<>]+)/g;
const LANGUAGE_TOKEN = /^([a-zA-Z0-9#+-]+)(?:[ \t]+|$)/;
const TRAILING_PUNCTUATION = /[.,;:!?]+$/;

const LANGUAGES = new Set([
  'java',
  'kotlin',
  'typescript',
  'ts',
  'javascript',
  'js',
  'python',
  'py',
  'sql',
  'bash',
  'sh',
  'shell',
  'json',
  'xml',
  'html',
  'css',
  'scss',
  'yaml',
  'yml',
  'c',
  'cpp',
  'csharp',
  'go',
  'rust',
]);

export function segment(content: string): Segment[] {
  const segments: Segment[] = [];
  let position = 0;

  for (const fence of content.matchAll(FENCE)) {
    const before = content.slice(position, fence.index);

    if (before.trim().length > 0) {
      segments.push({ type: 'text', value: before.trim() });
    }

    const code = toCode(fence[1], fence[2]);

    if (code !== null) {
      segments.push(code);
    } else if (fence[0].trim().length > 0) {
      segments.push({ type: 'text', value: fence[0].trim() });
    }

    position = fence.index + fence[0].length;
  }

  const rest = content.slice(position);

  if (rest.trim().length > 0) {
    segments.push({ type: 'text', value: rest.trim() });
  }

  return segments;
}

function toCode(firstLine: string, body: string): CodeSegment | null {
  let language = '';
  let prefix = firstLine;

  const token = LANGUAGE_TOKEN.exec(firstLine);

  if (token !== null && LANGUAGES.has(token[1].toLowerCase())) {
    language = token[1].toLowerCase();
    prefix = firstLine.slice(token[0].length);
  }

  const withoutTrailingNewline = body.replace(/\r?\n$/, '');
  const value = prefix.trim().length > 0
    ? [prefix, withoutTrailingNewline].filter((part) => part.length > 0).join('\n')
    : withoutTrailingNewline;

  if (value.trim().length === 0) {
    return null;
  }

  return { type: 'code', language, value };
}

export function chunksOf(text: string): Chunk[] {
  const chunks: Chunk[] = [];
  let position = 0;

  for (const match of text.matchAll(INLINE_OR_LINK)) {
    if (match.index > position) {
      chunks.push({ type: 'text', value: text.slice(position, match.index) });
    }

    if (match[1] !== undefined) {
      chunks.push({ type: 'inline-code', value: match[1] });
      position = match.index + match[0].length;
    } else {
      const link = trimLink(match[2]);
      chunks.push({ type: 'link', value: link });
      position = match.index + link.length;
    }
  }

  if (position < text.length) {
    chunks.push({ type: 'text', value: text.slice(position) });
  }

  return chunks;
}

function trimLink(raw: string): string {
  let link = raw.replace(TRAILING_PUNCTUATION, '');

  while (link.endsWith(')') && count(link, '(') < count(link, ')')) {
    link = link.slice(0, -1).replace(TRAILING_PUNCTUATION, '');
  }

  return link;
}

function count(text: string, character: string): number {
  return text.split(character).length - 1;
}
