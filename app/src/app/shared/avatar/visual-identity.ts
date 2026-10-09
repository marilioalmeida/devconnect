export function hueOf(name: string): number {
  let hash = 0;

  for (const character of name) {
    hash = (hash * 31 + character.codePointAt(0)!) % 360;
  }

  return hash;
}

export function initialsOf(name: string): string {
  const parts = name.trim().split(/\s+/).filter(Boolean);

  if (parts.length === 0) {
    return '?';
  }

  return parts
    .slice(0, 2)
    .map((part) => part[0].toUpperCase())
    .join('');
}
