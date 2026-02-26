import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { KudoTable } from '../KudoTable';
import {
  createMockKudoList,
  createMockKudoItem,
} from '../../../__tests__/mocks/kudosListMocks';

describe('KudoTable', () => {
  it('renderiza headers de columna correctos', () => {
    render(<KudoTable items={[]} />);

    expect(screen.getByText('De')).toBeDefined();
    expect(screen.getByText('Para')).toBeDefined();
    expect(screen.getByText('Categoría')).toBeDefined();
    expect(screen.getByText('Mensaje')).toBeDefined();
    expect(screen.getByText('Fecha')).toBeDefined();
  });

  it('renderiza filas con datos de kudos', () => {
    const items = createMockKudoList(3);
    render(<KudoTable items={items} />);

    items.forEach((item) => {
      expect(screen.getByText(item.fromUser)).toBeDefined();
      expect(screen.getByText(item.toUser)).toBeDefined();
    });
  });

  it('muestra badges de categoría', () => {
    const items = [
      createMockKudoItem({ category: 'Innovation' }, 0),
      createMockKudoItem({ category: 'Teamwork' }, 1),
      createMockKudoItem({ category: 'Passion' }, 2),
      createMockKudoItem({ category: 'Mastery' }, 3),
    ];
    render(<KudoTable items={items} />);

    expect(screen.getByText('Innovation')).toBeDefined();
    expect(screen.getByText('Teamwork')).toBeDefined();
    expect(screen.getByText('Passion')).toBeDefined();
    expect(screen.getByText('Mastery')).toBeDefined();
  });

  it('trunca mensajes largos (>100 chars) con "..."', () => {
    const longMessage = 'A'.repeat(150);
    const items = [createMockKudoItem({ message: longMessage }, 0)];
    render(<KudoTable items={items} />);

    const truncated = screen.getByText(/^A{100}\.\.\.$/);
    expect(truncated).toBeDefined();
  });

  it('no trunca mensajes cortos', () => {
    const shortMessage = 'Excelente trabajo';
    const items = [createMockKudoItem({ message: shortMessage }, 0)];
    render(<KudoTable items={items} />);

    expect(screen.getByText(shortMessage)).toBeDefined();
  });

  it('renderiza tabla vacía sin filas cuando items es []', () => {
    const { container } = render(<KudoTable items={[]} />);

    const rows = container.querySelectorAll('tbody tr');
    expect(rows.length).toBe(0);
  });

  it('usa HTML semántico con scope="col" en headers', () => {
    const { container } = render(<KudoTable items={[]} />);

    const ths = container.querySelectorAll('th[scope="col"]');
    expect(ths.length).toBe(5);
  });

  it('muestra email enmascarado tal como viene del backend', () => {
    const items = [
      createMockKudoItem({
        fromUser: 'j***z@sofkianos.com',
        toUser: 'a***n@sofkianos.com',
      }, 0),
    ];
    render(<KudoTable items={items} />);

    expect(screen.getByText('j***z@sofkianos.com')).toBeDefined();
    expect(screen.getByText('a***n@sofkianos.com')).toBeDefined();
  });
});
