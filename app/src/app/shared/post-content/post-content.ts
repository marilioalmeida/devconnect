import { Component, computed, input } from '@angular/core';
import { Segment, Chunk, segment, chunksOf } from './content-parser';
import { CodeBlock } from './code-block';

interface RenderedText {
  type: 'text';
  chunks: Chunk[];
}

interface RenderedCode {
  type: 'code';
  language: string;
  value: string;
}

type Part = RenderedText | RenderedCode;

@Component({
  selector: 'app-post-content',
  imports: [CodeBlock],
  templateUrl: './post-content.html',
  styleUrl: './post-content.scss',
})
export class PostContent {
  readonly content = input.required<string>();
  readonly inlineOnly = input(false);

  protected readonly parts = computed<Part[]>(() => {
    if (this.inlineOnly()) {
      return [{ type: 'text', chunks: chunksOf(this.content()) }];
    }

    return segment(this.content()).map(toPart);
  });
}

function toPart(segment: Segment): Part {
  if (segment.type === 'code') {
    return { type: 'code', language: segment.language, value: segment.value };
  }

  return { type: 'text', chunks: chunksOf(segment.value) };
}
