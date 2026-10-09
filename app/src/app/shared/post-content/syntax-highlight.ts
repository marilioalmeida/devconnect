type HljsModule = typeof import('highlight.js/lib/core').default;

let loading: Promise<HljsModule> | null = null;

async function load(): Promise<HljsModule> {
  const [core, java, typescript, javascript, python, sql, bash, json, xml, css] =
    await Promise.all([
      import('highlight.js/lib/core'),
      import('highlight.js/lib/languages/java'),
      import('highlight.js/lib/languages/typescript'),
      import('highlight.js/lib/languages/javascript'),
      import('highlight.js/lib/languages/python'),
      import('highlight.js/lib/languages/sql'),
      import('highlight.js/lib/languages/bash'),
      import('highlight.js/lib/languages/json'),
      import('highlight.js/lib/languages/xml'),
      import('highlight.js/lib/languages/css'),
    ]);

  const hljs = core.default;

  hljs.registerLanguage('java', java.default);
  hljs.registerLanguage('typescript', typescript.default);
  hljs.registerLanguage('javascript', javascript.default);
  hljs.registerLanguage('python', python.default);
  hljs.registerLanguage('sql', sql.default);
  hljs.registerLanguage('bash', bash.default);
  hljs.registerLanguage('json', json.default);
  hljs.registerLanguage('xml', xml.default);
  hljs.registerLanguage('css', css.default);
  hljs.registerAliases(['js'], { languageName: 'javascript' });
  hljs.registerAliases(['ts'], { languageName: 'typescript' });
  hljs.registerAliases(['sh', 'shell'], { languageName: 'bash' });
  hljs.registerAliases(['html'], { languageName: 'xml' });
  hljs.registerAliases(['py'], { languageName: 'python' });

  return hljs;
}

export async function highlight(code: string, language: string): Promise<string | null> {
  loading ??= load();

  try {
    const hljs = await loading;

    if (!hljs.getLanguage(language)) {
      return null;
    }

    return hljs.highlight(code, { language: language }).value;
  } catch {
    return null;
  }
}
