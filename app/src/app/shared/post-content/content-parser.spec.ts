import { segment, chunksOf } from './content-parser';

describe('segment', () => {
  it('returns a single text segment when there is no code fence', () => {
    expect(segment('Just a regular post.')).toEqual([
      { type: 'text', value: 'Just a regular post.' },
    ]);
  });

  it('splits text and a code block with a language', () => {
    const segments = segment('Look at this:\n```java\nvar x = 1;\n```\nNice, right?');

    expect(segments).toEqual([
      { type: 'text', value: 'Look at this:' },
      { type: 'code', language: 'java', value: 'var x = 1;' },
      { type: 'text', value: 'Nice, right?' },
    ]);
  });

  it('accepts a fence without a language', () => {
    const segments = segment('```\nplain\n```');

    expect(segments).toEqual([{ type: 'code', language: '', value: 'plain' }]);
  });

  it('normalizes the language to lowercase', () => {
    expect(segment('```SQL\nSELECT 1;\n```')[0]).toEqual({
      type: 'code',
      language: 'sql',
      value: 'SELECT 1;',
    });
  });

  it('extends an unclosed fence to the end of the content', () => {
    expect(segment('Start\n```java\nno closing fence')).toEqual([
      { type: 'text', value: 'Start' },
      { type: 'code', language: 'java', value: 'no closing fence' },
    ]);
  });

  it('accepts code starting on the same line as the fence', () => {
    const segments = segment('```java mockMvc.perform(post("/login")\n    .andExpect(ok());\n```');

    expect(segments).toEqual([
      {
        type: 'code',
        language: 'java',
        value: 'mockMvc.perform(post("/login")\n    .andExpect(ok());',
      },
    ]);
  });

  it('does not mistake a regular word at the start of the block for a language', () => {
    expect(segment('```var x = 1;```')).toEqual([
      { type: 'code', language: '', value: 'var x = 1;' },
    ]);
  });

  it('keeps the raw text when the fence has no code', () => {
    expect(segment('```java')).toEqual([{ type: 'text', value: '```java' }]);
  });

  it('splits multiple blocks in the same post', () => {
    const segments = segment('```js\na();\n```\nmiddle\n```sql\nSELECT 1;\n```');

    expect(segments.map((segment) => segment.type)).toEqual(['code', 'text', 'code']);
  });

  it('keeps blank lines inside the block', () => {
    const segments = segment('```java\nline1;\n\nline2;\n```');

    expect(segments[0].value).toBe('line1;\n\nline2;');
  });
});

describe('chunksOf', () => {
  it('returns the whole text when there is no inline code or link', () => {
    expect(chunksOf('plain text')).toEqual([{ type: 'text', value: 'plain text' }]);
  });

  it('extracts inline code in the middle of the text', () => {
    expect(chunksOf('always run `mvn test` first')).toEqual([
      { type: 'text', value: 'always run ' },
      { type: 'inline-code', value: 'mvn test' },
      { type: 'text', value: ' first' },
    ]);
  });

  it('extracts a link in the middle of the text', () => {
    expect(chunksOf('see https://angular.dev and then')).toEqual([
      { type: 'text', value: 'see ' },
      { type: 'link', value: 'https://angular.dev' },
      { type: 'text', value: ' and then' },
    ]);
  });

  it('combines inline code and a link in the same sentence', () => {
    const chunks = chunksOf('run `ng serve` and open http://localhost:4200 in the browser');

    expect(chunks.map((chunk) => chunk.type)).toEqual([
      'text',
      'inline-code',
      'text',
      'link',
      'text',
    ]);
  });

  it('does not treat an unpaired backtick as code', () => {
    expect(chunksOf('an `unclosed backtick')).toEqual([
      { type: 'text', value: 'an `unclosed backtick' },
    ]);
  });

  it('keeps balanced parentheses inside the link', () => {
    const url = 'https://en.wikipedia.org/wiki/Java_(programming_language)';

    expect(chunksOf(`see ${url} later`)).toEqual([
      { type: 'text', value: 'see ' },
      { type: 'link', value: url },
      { type: 'text', value: ' later' },
    ]);
  });

  it('does not swallow the parenthesis that wraps the link', () => {
    expect(chunksOf('(see https://angular.dev)')).toEqual([
      { type: 'text', value: '(see ' },
      { type: 'link', value: 'https://angular.dev' },
      { type: 'text', value: ')' },
    ]);
  });

  it('leaves trailing punctuation out of the link', () => {
    expect(chunksOf('visit https://angular.dev.')).toEqual([
      { type: 'text', value: 'visit ' },
      { type: 'link', value: 'https://angular.dev' },
      { type: 'text', value: '.' },
    ]);
  });
});
