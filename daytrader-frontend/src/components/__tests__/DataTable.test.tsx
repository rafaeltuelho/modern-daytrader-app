import { describe, it, expect, vi } from 'vitest';
import { render, screen } from '../../__tests__/test-utils';
import userEvent from '@testing-library/user-event';
import { DataTable, Column } from '../DataTable';

interface TestData {
  id: number;
  name: string;
  value: number;
}

const mockData: TestData[] = [
  { id: 1, name: 'Item A', value: 100 },
  { id: 2, name: 'Item B', value: 200 },
  { id: 3, name: 'Item C', value: 150 },
];

const columns: Column<TestData>[] = [
  { id: 'id', label: 'ID' },
  { id: 'name', label: 'Name' },
  { id: 'value', label: 'Value', align: 'right' },
];

describe('DataTable', () => {
  it('renders table with data', () => {
    render(<DataTable columns={columns} data={mockData} />);
    
    expect(screen.getByText('ID')).toBeInTheDocument();
    expect(screen.getByText('Name')).toBeInTheDocument();
    expect(screen.getByText('Value')).toBeInTheDocument();
    
    expect(screen.getByText('Item A')).toBeInTheDocument();
    expect(screen.getByText('Item B')).toBeInTheDocument();
    expect(screen.getByText('Item C')).toBeInTheDocument();
  });

  it('renders empty message when no data', () => {
    render(<DataTable columns={columns} data={[]} />);
    expect(screen.getByText('No data available')).toBeInTheDocument();
  });

  it('renders custom empty message', () => {
    render(<DataTable columns={columns} data={[]} emptyMessage="No items found" />);
    expect(screen.getByText('No items found')).toBeInTheDocument();
  });

  it('renders custom cell content with render function', () => {
    const customColumns: Column<TestData>[] = [
      { id: 'name', label: 'Name' },
      { 
        id: 'value', 
        label: 'Value', 
        render: (row) => `$${row.value.toFixed(2)}` 
      },
    ];
    
    render(<DataTable columns={customColumns} data={mockData} />);
    expect(screen.getByText('$100.00')).toBeInTheDocument();
    expect(screen.getByText('$200.00')).toBeInTheDocument();
  });

  it('sorts data when column header is clicked', async () => {
    const user = userEvent.setup();
    render(<DataTable columns={columns} data={mockData} />);
    
    const nameHeader = screen.getByText('Name');
    await user.click(nameHeader);
    
    const rows = screen.getAllByRole('row');
    // First row is header, so data rows start at index 1
    expect(rows[1]).toHaveTextContent('Item A');
    expect(rows[2]).toHaveTextContent('Item B');
    expect(rows[3]).toHaveTextContent('Item C');
  });

  it('reverses sort order on second click', async () => {
    const user = userEvent.setup();
    render(<DataTable columns={columns} data={mockData} />);
    
    const nameHeader = screen.getByText('Name');
    await user.click(nameHeader); // First click - ascending
    await user.click(nameHeader); // Second click - descending
    
    const rows = screen.getAllByRole('row');
    expect(rows[1]).toHaveTextContent('Item C');
    expect(rows[2]).toHaveTextContent('Item B');
    expect(rows[3]).toHaveTextContent('Item A');
  });

  it('calls onRowClick when row is clicked', async () => {
    const user = userEvent.setup();
    const onRowClick = vi.fn();
    render(<DataTable columns={columns} data={mockData} onRowClick={onRowClick} />);
    
    const firstRow = screen.getByText('Item A').closest('tr');
    if (firstRow) {
      await user.click(firstRow);
      expect(onRowClick).toHaveBeenCalledWith(mockData[0]);
    }
  });

  it('does not make rows clickable when onRowClick is not provided', () => {
    const { container } = render(<DataTable columns={columns} data={mockData} />);
    const rows = container.querySelectorAll('tbody tr');
    rows.forEach(row => {
      expect(row).toHaveStyle({ cursor: 'default' });
    });
  });

  it('makes rows clickable when onRowClick is provided', () => {
    const onRowClick = vi.fn();
    const { container } = render(<DataTable columns={columns} data={mockData} onRowClick={onRowClick} />);
    const rows = container.querySelectorAll('tbody tr');
    rows.forEach(row => {
      expect(row).toHaveStyle({ cursor: 'pointer' });
    });
  });

  it('disables sorting for columns with sortable: false', () => {
    const columnsWithNonSortable: Column<TestData>[] = [
      { id: 'id', label: 'ID', sortable: false },
      { id: 'name', label: 'Name' },
    ];
    
    const { container } = render(<DataTable columns={columnsWithNonSortable} data={mockData} />);
    const idHeader = screen.getByText('ID');
    const sortLabel = idHeader.closest('.MuiTableSortLabel-root');
    expect(sortLabel).not.toBeInTheDocument();
  });
});

