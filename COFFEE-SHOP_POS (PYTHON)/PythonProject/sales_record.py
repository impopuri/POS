import sys
import pandas as pd
from PyQt5.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QPushButton, QLabel, QTableWidget, QTableWidgetItem, QHeaderView,
    QComboBox, QDateEdit, QMessageBox, QFrame, QSplitter
)
from PyQt5.QtCore import Qt, QDate
from PyQt5.QtGui import QFont, QPalette, QColor
from datetime import datetime, timedelta
import os
from collections import defaultdict


class SalesRecordWindow(QMainWindow):
    def __init__(self, parent=None):
        super().__init__(parent)
        self.parent_window = parent
        self.setup_ui()
        self.setup_styles()
        self.load_sales_data()

    def setup_ui(self):
        self.setWindowTitle("Sales Records - Cafe POS")
        self.setGeometry(200, 100, 1000, 700)

        # Central widget
        central_widget = QWidget()
        self.setCentralWidget(central_widget)

        # Main layout
        main_layout = QVBoxLayout()
        central_widget.setLayout(main_layout)

        # Header section
        header_layout = QHBoxLayout()

        title = QLabel("SALES RECORDS")
        title.setFont(QFont("Arial", 24, QFont.Bold))

        # Back button
        self.back_btn = QPushButton("← Back to POS")
        self.back_btn.setFixedSize(120, 40)
        self.back_btn.clicked.connect(self.close)

        header_layout.addWidget(title)
        header_layout.addStretch()
        header_layout.addWidget(self.back_btn)

        # Filter section
        filter_frame = QFrame()
        filter_frame.setFrameStyle(QFrame.Box)
        filter_layout = QHBoxLayout()

        # Date filter
        date_label = QLabel("Filter by Date:")
        self.date_filter = QComboBox()
        self.date_filter.addItems([
            "All Time", "Today", "Yesterday", "Last 7 Days",
            "Last 30 Days", "This Month", "Custom Range"
        ])
        self.date_filter.currentTextChanged.connect(self.filter_changed)

        # Product filter
        product_label = QLabel("Filter by Product:")
        self.product_filter = QComboBox()
        self.product_filter.addItem("All Products")
        self.product_filter.currentTextChanged.connect(self.filter_changed)

        # Refresh button
        self.refresh_btn = QPushButton("Refresh Data")
        self.refresh_btn.clicked.connect(self.load_sales_data)

        filter_layout.addWidget(date_label)
        filter_layout.addWidget(self.date_filter)
        filter_layout.addWidget(product_label)
        filter_layout.addWidget(self.product_filter)
        filter_layout.addWidget(self.refresh_btn)
        filter_layout.addStretch()

        filter_frame.setLayout(filter_layout)

        # Create splitter for summary and detailed view
        splitter = QSplitter(Qt.Horizontal)

        # Summary section (left side)
        summary_widget = self.create_summary_section()
        splitter.addWidget(summary_widget)

        # Detailed records section (right side)
        records_widget = self.create_records_section()
        splitter.addWidget(records_widget)

        # Set splitter proportions
        splitter.setSizes([400, 600])

        # Add all to main layout
        main_layout.addLayout(header_layout)
        main_layout.addWidget(filter_frame)
        main_layout.addWidget(splitter)

    def create_summary_section(self):
        summary_widget = QWidget()
        summary_layout = QVBoxLayout()

        # Summary title
        summary_title = QLabel("SALES SUMMARY")
        summary_title.setFont(QFont("Arial", 16, QFont.Bold))
        summary_title.setAlignment(Qt.AlignCenter)

        # Summary table
        self.summary_table = QTableWidget()
        self.summary_table.setColumnCount(3)
        self.summary_table.setHorizontalHeaderLabels(["Product", "Qty Sold", "Total Sales"])

        # Set column widths
        header = self.summary_table.horizontalHeader()
        header.setSectionResizeMode(0, QHeaderView.Stretch)
        header.setSectionResizeMode(1, QHeaderView.ResizeToContents)
        header.setSectionResizeMode(2, QHeaderView.ResizeToContents)

        # Total sales label
        self.total_sales_label = QLabel("TOTAL SALES: ₱0.00")
        self.total_sales_label.setFont(QFont("Arial", 14, QFont.Bold))
        self.total_sales_label.setAlignment(Qt.AlignCenter)
        self.total_sales_label.setStyleSheet(
            "background-color: #3B2621; color: white; padding: 10px; border-radius: 5px;")

        summary_layout.addWidget(summary_title)
        summary_layout.addWidget(self.summary_table)
        summary_layout.addWidget(self.total_sales_label)

        summary_widget.setLayout(summary_layout)
        return summary_widget

    def create_records_section(self):
        records_widget = QWidget()
        records_layout = QVBoxLayout()

        # Records title
        records_title = QLabel("DETAILED RECORDS")
        records_title.setFont(QFont("Arial", 16, QFont.Bold))
        records_title.setAlignment(Qt.AlignCenter)

        # Records table
        self.records_table = QTableWidget()
        self.records_table.setColumnCount(6)
        self.records_table.setHorizontalHeaderLabels([
            "Date/Time", "Customer", "Product", "Quantity", "Unit Price", "Total"
        ])

        # Set column widths
        header = self.records_table.horizontalHeader()
        header.setSectionResizeMode(0, QHeaderView.ResizeToContents)
        header.setSectionResizeMode(1, QHeaderView.Stretch)
        header.setSectionResizeMode(2, QHeaderView.Stretch)
        header.setSectionResizeMode(3, QHeaderView.ResizeToContents)
        header.setSectionResizeMode(4, QHeaderView.ResizeToContents)
        header.setSectionResizeMode(5, QHeaderView.ResizeToContents)

        # Export button
        self.export_btn = QPushButton("Export to Excel")
        self.export_btn.clicked.connect(self.export_to_excel)

        records_layout.addWidget(records_title)
        records_layout.addWidget(self.records_table)
        records_layout.addWidget(self.export_btn)

        records_widget.setLayout(records_layout)
        return records_widget

    def setup_styles(self):
        self.setStyleSheet("""
            QMainWindow {
                background-color: #DACCBA;
            }

            QPushButton {
                background-color: #895B4A;
                color: white;
                border: none;
                border-radius: 8px;
                font-size: 12px;
                font-weight: bold;
                padding: 8px 16px;
            }

            QPushButton:hover {
                background-color: #6b4437;
            }

            QPushButton:pressed {
                background-color: #FFA500;
            }

            QLabel {
                color: #3B2621;
            }

            QTableWidget {
                background-color: white;
                border: 1px solid #ccc;
                border-radius: 5px;
                gridline-color: #ddd;
            }

            QTableWidget::item {
                padding: 8px;
                border-bottom: 1px solid #eee;
            }

            QTableWidget::item:selected {
                background-color: #FFA500;
                color: white;
            }

            QHeaderView::section {
                background-color: #3B2621;
                color: white;
                padding: 8px;
                border: none;
                font-weight: bold;
            }

            QComboBox {
                padding: 5px;
                border: 1px solid #ccc;
                border-radius: 4px;
                background-color: white;
                min-width: 100px;
            }

            QFrame {
                background-color: rgba(255, 255, 255, 0.8);
                border-radius: 5px;
                padding: 10px;
                margin: 5px;
            }
        """)

    def load_sales_data(self):
        """Load sales data from CSV file"""
        try:
            if not os.path.exists('order-summary.csv'):
                # Create sample data if file doesn't exist
                self.create_sample_data()

            # Read CSV file
            self.df = pd.read_csv('order-summary.csv',
                                  names=['timestamp', 'orders', 'total', 'received', 'change', 'customer'])

            # Convert timestamp to datetime
            self.df['timestamp'] = pd.to_datetime(self.df['timestamp'])

            # Process the data to extract individual items
            self.process_order_data()

            # Update product filter
            self.update_product_filter()

            # Apply current filters
            self.filter_changed()

        except Exception as e:
            QMessageBox.warning(self, "Error", f"Error loading sales data: {str(e)}")
            self.create_empty_tables()

    def create_sample_data(self):
        """Create sample data for demonstration"""
        sample_data = [
            {
                'timestamp': datetime.now() - timedelta(days=1),
                'orders': "['Hot Americano Reg1x', 'Cold Latte Lrg2x']",
                'total': 230.0,
                'received': 250.0,
                'change': 20.0,
                'customer': 'John Doe'
            },
            {
                'timestamp': datetime.now() - timedelta(hours=2),
                'orders': "['Hot Cappuccino Reg1x']",
                'total': 110.0,
                'received': 110.0,
                'change': 0.0,
                'customer': 'Jane Smith'
            }
        ]

        df = pd.DataFrame(sample_data)
        df.to_csv('order-summary.csv', mode='w', header=False, index=False)

    def process_order_data(self):
        """Process order data to extract individual items"""
        self.processed_data = []

        for _, row in self.df.iterrows():
            try:
                # Parse orders string (it's stored as string representation of list)
                orders_str = row['orders'].strip("[]'")
                orders = [item.strip("' ") for item in orders_str.split("', '")]

                for order in orders:
                    if order:  # Skip empty orders
                        # Extract product info from order string
                        # Format: "Hot Americano Reg1x" or "Cold Latte Lrg2x"
                        item_info = self.parse_order_item(order)
                        if item_info:
                            item_info.update({
                                'timestamp': row['timestamp'],
                                'customer': row['customer'] if pd.notna(row['customer']) else 'Walk-in'
                            })
                            self.processed_data.append(item_info)
            except Exception as e:
                print(f"Error processing order: {e}")
                continue

    def parse_order_item(self, order_str):
        """Parse individual order item string"""
        try:
            # Extract quantity (last part with 'x')
            if 'x' in order_str:
                parts = order_str.rsplit('x', 1)
                quantity_part = parts[-1]
                if quantity_part.isdigit():
                    quantity = int(quantity_part)
                    item_part = parts[0] + 'x'
                else:
                    quantity = 1
                    item_part = order_str
            else:
                quantity = 1
                item_part = order_str

            # Remove quantity from item name for display
            if item_part.endswith('x'):
                item_name = item_part[:-1]
            else:
                item_name = item_part

            # Extract base product name and calculate price
            base_price = self.get_base_price(item_name)
            unit_price = self.calculate_unit_price(item_name, base_price)
            total_price = unit_price * quantity

            return {
                'product': item_name.strip(),
                'quantity': quantity,
                'unit_price': unit_price,
                'total_price': total_price
            }
        except Exception as e:
            print(f"Error parsing order item: {e}")
            return None

    def get_base_price(self, item_name):
        """Get base price for product"""
        price_map = {
            'Americano': 100,
            'Cappuccino': 110,
            'CafeLatte': 110,
            'SpLatte': 120,
            'Matcha': 120,
            'Chocolate': 120
        }

        for product, price in price_map.items():
            if product in item_name:
                return price
        return 100  # Default price

    def calculate_unit_price(self, item_name, base_price):
        """Calculate unit price including size and temperature modifiers"""
        price = base_price
        if 'Lrg' in item_name:
            price += 10
        if 'Cold' in item_name:
            price += 5
        return price

    def update_product_filter(self):
        """Update product filter dropdown"""
        self.product_filter.clear()
        self.product_filter.addItem("All Products")

        if hasattr(self, 'processed_data'):
            products = set([item['product'] for item in self.processed_data])
            for product in sorted(products):
                self.product_filter.addItem(product)

    def filter_changed(self):
        """Apply filters to data"""
        if not hasattr(self, 'processed_data'):
            return

        filtered_data = self.processed_data.copy()

        # Apply date filter
        date_filter = self.date_filter.currentText()
        if date_filter != "All Time":
            filtered_data = self.apply_date_filter(filtered_data, date_filter)

        # Apply product filter
        product_filter = self.product_filter.currentText()
        if product_filter != "All Products":
            filtered_data = [item for item in filtered_data if item['product'] == product_filter]

        # Update tables
        self.update_summary_table(filtered_data)
        self.update_records_table(filtered_data)

    def apply_date_filter(self, data, date_filter):
        """Apply date filter to data"""
        now = datetime.now()

        if date_filter == "Today":
            start_date = now.replace(hour=0, minute=0, second=0, microsecond=0)
            return [item for item in data if item['timestamp'] >= start_date]
        elif date_filter == "Yesterday":
            yesterday = now - timedelta(days=1)
            start_date = yesterday.replace(hour=0, minute=0, second=0, microsecond=0)
            end_date = now.replace(hour=0, minute=0, second=0, microsecond=0)
            return [item for item in data if start_date <= item['timestamp'] < end_date]
        elif date_filter == "Last 7 Days":
            start_date = now - timedelta(days=7)
            return [item for item in data if item['timestamp'] >= start_date]
        elif date_filter == "Last 30 Days":
            start_date = now - timedelta(days=30)
            return [item for item in data if item['timestamp'] >= start_date]
        elif date_filter == "This Month":
            start_date = now.replace(day=1, hour=0, minute=0, second=0, microsecond=0)
            return [item for item in data if item['timestamp'] >= start_date]

        return data

    def update_summary_table(self, data):
        """Update summary table with filtered data"""
        # Aggregate data by product
        summary = defaultdict(lambda: {'quantity': 0, 'total': 0})

        for item in data:
            product = item['product']
            summary[product]['quantity'] += item['quantity']
            summary[product]['total'] += item['total_price']

        # Update table
        self.summary_table.setRowCount(len(summary))

        total_sales = 0
        for row, (product, stats) in enumerate(sorted(summary.items())):
            self.summary_table.setItem(row, 0, QTableWidgetItem(product))
            self.summary_table.setItem(row, 1, QTableWidgetItem(str(stats['quantity'])))
            self.summary_table.setItem(row, 2, QTableWidgetItem(f"₱{stats['total']:.2f}"))
            total_sales += stats['total']

        # Update total sales label
        self.total_sales_label.setText(f"TOTAL SALES: ₱{total_sales:.2f}")

    def update_records_table(self, data):
        """Update detailed records table"""
        self.records_table.setRowCount(len(data))

        for row, item in enumerate(sorted(data, key=lambda x: x['timestamp'], reverse=True)):
            self.records_table.setItem(row, 0, QTableWidgetItem(
                item['timestamp'].strftime('%Y-%m-%d %H:%M:%S')
            ))
            self.records_table.setItem(row, 1, QTableWidgetItem(item['customer']))
            self.records_table.setItem(row, 2, QTableWidgetItem(item['product']))
            self.records_table.setItem(row, 3, QTableWidgetItem(str(item['quantity'])))
            self.records_table.setItem(row, 4, QTableWidgetItem(f"₱{item['unit_price']:.2f}"))
            self.records_table.setItem(row, 5, QTableWidgetItem(f"₱{item['total_price']:.2f}"))

    def create_empty_tables(self):
        """Create empty tables when no data is available"""
        self.summary_table.setRowCount(0)
        self.records_table.setRowCount(0)
        self.total_sales_label.setText("TOTAL SALES: ₱0.00")

    def export_to_excel(self):
        """Export current filtered data to Excel"""
        try:
            if not hasattr(self, 'processed_data'):
                QMessageBox.warning(self, "No Data", "No data available to export.")
                return

            # Get current filtered data
            filtered_data = self.get_current_filtered_data()

            if not filtered_data:
                QMessageBox.warning(self, "No Data", "No data matches the current filters.")
                return

            # Create DataFrame for export
            export_data = []
            for item in filtered_data:
                export_data.append({
                    'Date/Time': item['timestamp'].strftime('%Y-%m-%d %H:%M:%S'),
                    'Customer': item['customer'],
                    'Product': item['product'],
                    'Quantity': item['quantity'],
                    'Unit Price': item['unit_price'],
                    'Total Price': item['total_price']
                })

            df = pd.DataFrame(export_data)

            # Generate filename with timestamp
            filename = f"sales_report_{datetime.now().strftime('%Y%m%d_%H%M%S')}.xlsx"

            # Export to Excel
            df.to_excel(filename, index=False)

            QMessageBox.information(self, "Export Successful",
                                    f"Sales report exported to {filename}")

        except Exception as e:
            QMessageBox.warning(self, "Export Error", f"Error exporting data: {str(e)}")

    def get_current_filtered_data(self):
        """Get currently filtered data"""
        if not hasattr(self, 'processed_data'):
            return []

        filtered_data = self.processed_data.copy()

        # Apply current filters
        date_filter = self.date_filter.currentText()
        if date_filter != "All Time":
            filtered_data = self.apply_date_filter(filtered_data, date_filter)

        product_filter = self.product_filter.currentText()
        if product_filter != "All Products":
            filtered_data = [item for item in filtered_data if item['product'] == product_filter]

        return filtered_data


def main():
    """Main function for testing the sales record window independently"""
    app = QApplication(sys.argv)
    app.setStyle('Fusion')
    window = SalesRecordWindow()
    window.show()
    sys.exit(app.exec_())


if __name__ == '__main__':
    main()