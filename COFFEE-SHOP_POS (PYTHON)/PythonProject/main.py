import sys
import pandas as pd
from PyQt5.QtWidgets import (
    QApplication, QMainWindow, QWidget, QVBoxLayout, QHBoxLayout,
    QPushButton, QLabel, QLineEdit, QScrollArea, QFrame, QCheckBox,
    QSpacerItem, QSizePolicy, QMessageBox, QDialog, QTextEdit
)
from PyQt5.QtCore import Qt, pyqtSignal
from PyQt5.QtGui import QFont, QPalette, QColor
from PyQt5.QtPrintSupport import QPrintDialog, QPrinter
from datetime import datetime
# Import the sales record module
from sales_record import SalesRecordWindow


class ReceiptDialog(QDialog):
    back_clicked = pyqtSignal()

    def __init__(self, customer_name, order_items, total, received, change, parent=None):
        super().__init__(parent)
        self.customer_name = customer_name
        self.order_items = order_items
        self.total = total
        self.received = received
        self.change = change
        self.setup_ui()

    def setup_ui(self):
        self.setWindowTitle("Receipt")
        self.setFixedSize(400, 600)
        self.setStyleSheet("""
            QDialog {
                background-color: white;
            }
            QLabel {
                color: black;
            }
        """)

        layout = QVBoxLayout()

        # Receipt content
        self.receipt_text = QTextEdit()
        self.receipt_text.setReadOnly(True)
        self.receipt_text.setFont(QFont("Courier", 10))

        # Generate receipt content
        receipt_content = self.generate_receipt_content()
        self.receipt_text.setPlainText(receipt_content)

        # Buttons
        button_layout = QHBoxLayout()

        self.print_btn = QPushButton("Print Receipt")
        self.print_btn.setFixedSize(120, 40)
        self.print_btn.clicked.connect(self.print_receipt)

        self.back_btn = QPushButton("OK")
        self.back_btn.setFixedSize(80, 40)
        self.back_btn.clicked.connect(self.back_to_order)

        button_layout.addWidget(self.print_btn)
        button_layout.addWidget(self.back_btn)
        button_layout.addStretch()

        layout.addWidget(self.receipt_text)
        layout.addLayout(button_layout)

        self.setLayout(layout)

    def generate_receipt_content(self):
        receipt = []
        receipt.append("=" * 40)
        receipt.append("         CAFE RECEIPT")
        receipt.append("=" * 40)
        receipt.append(f"Date: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        receipt.append(f"Customer: {self.customer_name or 'Walk-in'}")
        receipt.append("-" * 40)
        receipt.append("ITEMS:")
        receipt.append("-" * 40)

        for item in self.order_items:
            receipt.append(f"{item.order_name}")
            receipt.append(f"  {item.order_qty} x ₱{item.order_price} = ₱{item.order_total}")
            receipt.append("")

        receipt.append("-" * 40)
        receipt.append(f"TOTAL:           ₱{self.total:.2f}")
        receipt.append(f"RECEIVED:        ₱{self.received:.2f}")
        receipt.append(f"CHANGE:          ₱{self.change:.2f}")
        receipt.append("=" * 40)
        receipt.append("    Thank you for your visit!")
        receipt.append("=" * 40)

        return "\n".join(receipt)

    def print_receipt(self):
        printer = QPrinter()
        print_dialog = QPrintDialog(printer, self)

        if print_dialog.exec_() == QPrintDialog.Accepted:
            self.receipt_text.print_(printer)
            QMessageBox.information(self, "Print", "Receipt printed successfully!")

    def back_to_order(self):
        self.back_clicked.emit()
        self.close()


class OrderItem(QFrame):
    delete_signal = pyqtSignal(object)

    def __init__(self, order_name, order_price, order_qty, order_total):
        super().__init__()
        self.order_name = order_name
        self.order_price = order_price
        self.order_qty = order_qty
        self.order_total = order_total
        self.setup_ui()

    def setup_ui(self):
        self.setFrameStyle(QFrame.Box)
        self.setStyleSheet("QFrame { border: 1px solid #ccc; margin: 2px; padding: 5px; }")

        layout = QHBoxLayout()

        # Order details
        self.name_label = QLabel(self.order_name)
        self.qty_label = QLabel(self.order_qty)
        self.price_label = QLabel(f"₱{self.order_price}")
        self.total_label = QLabel(f"₱{self.order_total}")

        # Delete button
        self.delete_btn = QPushButton("×")
        self.delete_btn.setFixedSize(25, 25)
        self.delete_btn.setStyleSheet("""
            QPushButton {
                background-color: #dc3545;
                color: white;
                border: none;
                border-radius: 12px;
                font-weight: bold;
            }
            QPushButton:hover {
                background-color: #c82333;
            }
        """)
        self.delete_btn.clicked.connect(self.delete_clicked)

        layout.addWidget(self.name_label)
        layout.addWidget(self.qty_label)
        layout.addWidget(self.price_label)
        layout.addWidget(self.total_label)
        layout.addWidget(self.delete_btn)

        self.setLayout(layout)

    def delete_clicked(self):
        self.delete_signal.emit(self)


class PreviousOrderItem(QFrame):
    def __init__(self, customer_name, order_items):
        super().__init__()
        self.customer_name = customer_name
        self.order_items = order_items
        self.completed = False
        self.setup_ui()

    def setup_ui(self):
        self.setFrameStyle(QFrame.Box)
        self.setStyleSheet("QFrame { border: 1px solid #ccc; margin: 2px; padding: 5px; }")

        layout = QVBoxLayout()

        # Checkbox and customer name
        checkbox_layout = QHBoxLayout()
        self.checkbox = QCheckBox()
        self.checkbox.stateChanged.connect(self.toggle_completion)

        self.customer_label = QLabel(self.customer_name.upper())
        self.customer_label.setFont(QFont("Arial", 10, QFont.Bold))

        checkbox_layout.addWidget(self.checkbox)
        checkbox_layout.addWidget(self.customer_label)

        # Order items
        self.items_label = QLabel("\n".join(self.order_items))
        self.items_label.setWordWrap(True)

        layout.addLayout(checkbox_layout)
        layout.addWidget(self.items_label)

        self.setLayout(layout)

    def toggle_completion(self, state):
        self.completed = state == Qt.Checked
        if self.completed:
            self.setStyleSheet("""
                QFrame { 
                    border: 1px solid #ccc; 
                    margin: 2px; 
                    padding: 5px; 
                    background-color: #f0f0f0;
                }
                QLabel { 
                    text-decoration: line-through; 
                    color: #666;
                }
            """)
        else:
            self.setStyleSheet("QFrame { border: 1px solid #ccc; margin: 2px; padding: 5px; }")


class CafePOSApp(QMainWindow):
    def __init__(self):
        super().__init__()
        self.init_variables()
        self.setup_ui()
        self.setup_styles()

    def init_variables(self):
        self.current_order = {
            'name': '',
            'base_price': 0,
            'size': '',
            'temp': '',
            'quantity': 1,
            'total_price': 0
        }

        self.order_items = []
        self.pay_amounts = []
        self.previous_orders = []

    def setup_ui(self):
        self.setWindowTitle("Cafe POS System")
        self.setGeometry(100, 100, 1200, 800)

        # Central widget
        central_widget = QWidget()
        self.setCentralWidget(central_widget)

        # Main layout
        main_layout = QHBoxLayout()
        central_widget.setLayout(main_layout)

        # Left section - Menu
        menu_section = self.create_menu_section()
        main_layout.addWidget(menu_section)

        # Center section - Order
        order_section = self.create_order_section()
        main_layout.addWidget(order_section)

        # Right section - Previous Orders
        previous_section = self.create_previous_orders_section()
        main_layout.addWidget(previous_section)

    def create_menu_section(self):
        menu_widget = QWidget()
        menu_layout = QVBoxLayout()

        # Drink buttons (first row) - NOW WITH PRICES
        drinks_row1 = QHBoxLayout()
        self.americano_btn = self.create_drink_button("Americano\n₱100", 100)
        self.cappuccino_btn = self.create_drink_button("Cappuccino\n₱110", 110)
        self.latte_btn = self.create_drink_button("CafeLatte\n₱110", 110)

        drinks_row1.addWidget(self.americano_btn)
        drinks_row1.addWidget(self.cappuccino_btn)
        drinks_row1.addWidget(self.latte_btn)

        # Drink buttons (second row) - NOW WITH PRICES
        drinks_row2 = QHBoxLayout()
        self.splatte_btn = self.create_drink_button("SpLatte\n₱120", 120)
        self.matcha_btn = self.create_drink_button("Matcha\n₱120", 120)
        self.chocolate_btn = self.create_drink_button("Chocolate\n₱120", 120)

        drinks_row2.addWidget(self.splatte_btn)
        drinks_row2.addWidget(self.matcha_btn)
        drinks_row2.addWidget(self.chocolate_btn)

        # Size buttons
        size_row = QHBoxLayout()
        self.reg_btn = self.create_size_temp_button("Reg\n(+₱0)", self.reg_clicked)
        self.lrg_btn = self.create_size_temp_button("Lrg\n(+₱10)", self.lrg_clicked)

        # Initially disabled
        self.reg_btn.setEnabled(False)
        self.lrg_btn.setEnabled(False)

        size_row.addWidget(self.reg_btn)
        size_row.addWidget(self.lrg_btn)

        # Temperature buttons (NEW)
        temp_row = QHBoxLayout()
        self.hot_btn = self.create_size_temp_button("Hot\n(+₱0)", self.hot_clicked)
        self.cold_btn = self.create_size_temp_button("Cold\n(+₱5)", self.cold_clicked)

        # Initially disabled
        self.hot_btn.setEnabled(False)
        self.cold_btn.setEnabled(False)

        temp_row.addWidget(self.hot_btn)
        temp_row.addWidget(self.cold_btn)

        # Quantity controls
        qty_row = QHBoxLayout()
        self.minus_btn = QPushButton("-")
        self.minus_btn.setFixedSize(40, 40)
        self.minus_btn.clicked.connect(self.minus_clicked)

        self.qty_input = QLineEdit("1")
        self.qty_input.setFixedSize(50, 40)
        self.qty_input.setAlignment(Qt.AlignCenter)
        self.qty_input.setReadOnly(True)

        self.plus_btn = QPushButton("+")
        self.plus_btn.setFixedSize(40, 40)
        self.plus_btn.clicked.connect(self.plus_clicked)

        self.add_btn = QPushButton("ADD")
        self.add_btn.setFixedSize(200, 50)
        self.add_btn.setEnabled(False)
        self.add_btn.clicked.connect(self.add_to_order)

        qty_row.addWidget(self.minus_btn)
        qty_row.addWidget(self.qty_input)
        qty_row.addWidget(self.plus_btn)
        qty_row.addWidget(self.add_btn)

        # Add all rows to menu layout
        menu_layout.addLayout(drinks_row1)
        menu_layout.addLayout(drinks_row2)
        menu_layout.addLayout(size_row)
        menu_layout.addLayout(temp_row)
        menu_layout.addLayout(qty_row)
        menu_layout.addStretch()

        menu_widget.setLayout(menu_layout)
        return menu_widget

    def create_drink_button(self, name_with_price, price):
        btn = QPushButton(name_with_price)
        btn.setFixedSize(125, 200)
        # Extract just the drink name for the callback
        drink_name = name_with_price.split('\n')[0]
        btn.clicked.connect(lambda: self.drink_clicked(drink_name, price, btn))
        return btn

    def create_size_temp_button(self, name, callback):
        btn = QPushButton(name)
        btn.setFixedSize(125, 100)
        btn.clicked.connect(callback)
        return btn

    def create_order_section(self):
        order_widget = QWidget()
        order_layout = QVBoxLayout()

        # Header with customer name
        header_layout = QHBoxLayout()
        order_title = QLabel("ORDER:")
        order_title.setFont(QFont("Arial", 24, QFont.Bold))

        self.customer_input = QLineEdit()
        self.customer_input.setPlaceholderText("Customer Name")
        self.customer_input.setFixedWidth(150)

        header_layout.addWidget(order_title)
        header_layout.addWidget(self.customer_input)
        header_layout.addStretch()

        # Order list
        self.order_scroll = QScrollArea()
        self.order_scroll.setFixedSize(350, 200)
        self.order_scroll.setWidgetResizable(True)

        self.order_list_widget = QWidget()
        self.order_list_layout = QVBoxLayout()
        self.order_list_widget.setLayout(self.order_list_layout)
        self.order_scroll.setWidget(self.order_list_widget)

        # Payment info
        self.total_label = QLabel("TOTAL: ₱0")
        self.total_label.setFont(QFont("Arial", 16, QFont.Bold))

        self.received_label = QLabel("RECEIVED: ₱0")
        self.received_label.setFont(QFont("Arial", 16, QFont.Bold))

        self.change_label = QLabel("CHANGE: ₱0")
        self.change_label.setFont(QFont("Arial", 16, QFont.Bold))

        # Payment controls
        payment_layout = QHBoxLayout()
        self.amount_input = QLineEdit()
        self.amount_input.setPlaceholderText("Amount received")
        self.amount_input.setFixedWidth(120)

        self.pay_btn = QPushButton("Pay")
        self.pay_btn.setFixedSize(100, 40)
        self.pay_btn.clicked.connect(self.process_payment)

        payment_layout.addWidget(self.amount_input)
        payment_layout.addWidget(self.pay_btn)
        payment_layout.addStretch()

        # Button row for Reset Order and Sales
        button_row = QHBoxLayout()

        self.reset_order_btn = QPushButton("Reset Order")
        self.reset_order_btn.setFixedSize(125, 50)
        self.reset_order_btn.clicked.connect(self.reset_order)

        self.sales_btn = QPushButton("Sales")
        self.sales_btn.setFixedSize(125, 50)
        self.sales_btn.clicked.connect(self.show_sales_record)

        button_row.addWidget(self.reset_order_btn)
        button_row.addWidget(self.sales_btn)

        # Add all to order layout
        order_layout.addLayout(header_layout)
        order_layout.addWidget(self.order_scroll)
        order_layout.addWidget(self.total_label)
        order_layout.addWidget(self.received_label)
        order_layout.addWidget(self.change_label)
        order_layout.addLayout(payment_layout)
        order_layout.addLayout(button_row)
        order_layout.addStretch()

        order_widget.setLayout(order_layout)
        return order_widget

    def create_previous_orders_section(self):
        previous_widget = QWidget()
        previous_layout = QVBoxLayout()

        # Title
        title = QLabel("Previous Orders")
        title.setFont(QFont("Arial", 24, QFont.Bold))

        # Scroll area for previous orders
        self.previous_scroll = QScrollArea()
        self.previous_scroll.setFixedSize(250, 520)
        self.previous_scroll.setWidgetResizable(True)

        self.previous_list_widget = QWidget()
        self.previous_list_layout = QVBoxLayout()
        self.previous_list_widget.setLayout(self.previous_list_layout)
        self.previous_scroll.setWidget(self.previous_list_widget)

        previous_layout.addWidget(title)
        previous_layout.addWidget(self.previous_scroll)
        previous_layout.addStretch()

        previous_widget.setLayout(previous_layout)
        return previous_widget

    def setup_styles(self):
        # Main window background
        self.setStyleSheet("""
            QMainWindow {
                background-color: #DACCBA;
            }

            QPushButton {
                background-color: #895B4A;
                color: white;
                border: none;
                border-radius: 8px;
                font-size: 14px;
                font-weight: bold;
            }

            QPushButton:hover {
                background-color: #6b4437;
            }

            QPushButton:pressed {
                background-color: #FFA500;
            }

            QPushButton:disabled {
                background-color: #cccccc;
                color: #666666;
            }

            QLabel {
                color: #3B2621;
            }

            QLineEdit {
                padding: 5px;
                border: 1px solid #ccc;
                border-radius: 4px;
                background-color: white;
            }
        """)

        # Special styling for specific buttons
        self.add_btn.setStyleSheet("""
            QPushButton {
                background-color: #3B2621;
                color: white;
                border-radius: 25px;
                font-size: 16px;
            }
            QPushButton:hover {
                background-color: #2a1b17;
            }
            QPushButton:disabled {
                background-color: #cccccc;
            }
        """)

        self.pay_btn.setStyleSheet("""
            QPushButton {
                background-color: #3B2621;
                color: white;
            }
        """)

        # Reset order button styling
        self.reset_order_btn.setStyleSheet("""
            QPushButton {
                background-color: #dc3545;
                color: white;
                font-size: 16px;
                border-radius: 8px;
            }
            QPushButton:hover {
                background-color: #c82333;
            }
            QPushButton:pressed {
                background-color: #a71e2a;
            }
        """)

        # Sales button styling
        self.sales_btn.setStyleSheet("""
            QPushButton {
                background-color: #28a745;
                color: white;
                font-size: 16px;
                border-radius: 8px;
            }
            QPushButton:hover {
                background-color: #218838;
            }
            QPushButton:pressed {
                background-color: #1e7e34;
            }
        """)

    def show_sales_record(self):
        """Show sales record window"""
        try:
            self.sales_window = SalesRecordWindow(self)
            self.sales_window.show()
        except Exception as e:
            QMessageBox.warning(self, "Error", f"Could not open sales record window: {str(e)}")

    def drink_clicked(self, name, price, button):
        # Reset all drink buttons but keep them enabled
        for btn in [self.americano_btn, self.cappuccino_btn, self.latte_btn,
                    self.splatte_btn, self.matcha_btn, self.chocolate_btn]:
            btn.setStyleSheet("""
                QPushButton {
                    background-color: #895B4A;
                    color: white;
                    border: none;
                    border-radius: 8px;
                    font-size: 14px;
                    font-weight: bold;
                }
            """)

        # Highlight selected button
        button.setStyleSheet("""
            QPushButton {
                background-color: #FFA500;
                color: white;
                border: none;
                border-radius: 8px;
                font-size: 14px;
                font-weight: bold;
            }
        """)

        # Enable size and temperature buttons
        self.reg_btn.setEnabled(True)
        self.lrg_btn.setEnabled(True)
        self.hot_btn.setEnabled(True)
        self.cold_btn.setEnabled(True)

        # Update current order
        self.current_order['name'] = name
        self.current_order['base_price'] = price

        self.update_add_button()

    def reg_clicked(self):
        self.toggle_size_button(self.reg_btn, "Reg", 0)

    def lrg_clicked(self):
        self.toggle_size_button(self.lrg_btn, "Lrg", 10)

    def toggle_size_button(self, button, size, extra_price):
        # Reset both size buttons
        self.reg_btn.setStyleSheet("")
        self.lrg_btn.setStyleSheet("")

        if self.current_order['size'] == size:
            # Deselect
            self.current_order['size'] = ''
        else:
            # Select
            button.setStyleSheet("background-color: #FFA500;")
            self.current_order['size'] = size

        self.update_add_button()

    def hot_clicked(self):
        self.toggle_temp_button(self.hot_btn, "Hot", 0)

    def cold_clicked(self):
        self.toggle_temp_button(self.cold_btn, "Cold", 5)

    def toggle_temp_button(self, button, temp, extra_price):
        # Reset both temperature buttons
        self.hot_btn.setStyleSheet("")
        self.cold_btn.setStyleSheet("")

        if self.current_order['temp'] == temp:
            # Deselect
            self.current_order['temp'] = ''
        else:
            # Select
            button.setStyleSheet("background-color: #FFA500;")
            self.current_order['temp'] = temp

        self.update_add_button()

    def minus_clicked(self):
        qty = int(self.qty_input.text())
        if qty > 1:
            qty -= 1
            self.qty_input.setText(str(qty))
            self.current_order['quantity'] = qty

    def plus_clicked(self):
        qty = int(self.qty_input.text())
        qty += 1
        self.qty_input.setText(str(qty))
        self.current_order['quantity'] = qty

    def update_add_button(self):
        # Enable ADD button only when drink, size, and temperature are selected
        self.add_btn.setEnabled(bool(self.current_order['name'] and
                                   self.current_order['size'] and
                                   self.current_order['temp']))

    def add_to_order(self):
        if not (self.current_order['name'] and self.current_order['size'] and self.current_order['temp']):
            return

        # Calculate price
        total_price = self.current_order['base_price']
        if self.current_order['size'] == 'Lrg':
            total_price += 10
        if self.current_order['temp'] == 'Cold':
            total_price += 5

        # Create order name
        order_name = f"{self.current_order['temp']} {self.current_order['name']} {self.current_order['size']}"
        quantity = self.current_order['quantity']
        total_item_price = total_price * quantity

        # Create order item
        order_item = OrderItem(order_name, str(total_price), f"{quantity}x", str(total_item_price))
        order_item.delete_signal.connect(self.remove_order_item)

        # Add to order list
        self.order_items.append(order_item)
        self.pay_amounts.append(total_item_price)
        self.order_list_layout.addWidget(order_item)

        # Update total
        self.update_total()

        # Reset current selection only (not drink buttons)
        self.reset_current_selection()

    def remove_order_item(self, item):
        # Find and remove from lists
        if item in self.order_items:
            index = self.order_items.index(item)
            self.order_items.remove(item)
            self.pay_amounts.pop(index)

        # Remove from layout
        self.order_list_layout.removeWidget(item)
        item.deleteLater()

        # Update total
        self.update_total()

    def reset_order(self):
        """Reset the entire current order"""
        reply = QMessageBox.question(
            self,
            'Reset Order',
            'Are you sure you want to reset the entire order? This will remove all items.',
            QMessageBox.Yes | QMessageBox.No,
            QMessageBox.No
        )

        if reply == QMessageBox.Yes:
            # Clear all order items
            for item in self.order_items:
                self.order_list_layout.removeWidget(item)
                item.deleteLater()

            self.order_items.clear()
            self.pay_amounts.clear()

            # Reset payment information
            self.amount_input.clear()
            self.total_label.setText("TOTAL: ₱0")
            self.received_label.setText("RECEIVED: ₱0")
            self.change_label.setText("CHANGE: ₱0")

            # Reset current selection
            self.reset_selection()

    def update_total(self):
        total = sum(self.pay_amounts)
        self.total_label.setText(f"TOTAL: ₱{total}")

    def reset_current_selection(self):
        """Reset only the current selection (size, temp, qty) but keep drink buttons enabled"""
        # Reset current order state
        self.current_order['size'] = ''
        self.current_order['temp'] = ''
        self.current_order['quantity'] = 1

        # Reset size and temperature buttons
        for btn in [self.reg_btn, self.lrg_btn]:
            btn.setStyleSheet("")

        for btn in [self.hot_btn, self.cold_btn]:
            btn.setStyleSheet("")

        # Reset quantity
        self.qty_input.setText("1")
        self.add_btn.setEnabled(False)

    def reset_selection(self):
        """Full reset including drink selection"""
        # Reset current order
        self.current_order = {
            'name': '',
            'base_price': 0,
            'size': '',
            'temp': '',
            'quantity': 1,
            'total_price': 0
        }

        # Reset UI
        for btn in [self.americano_btn, self.cappuccino_btn, self.latte_btn,
                    self.splatte_btn, self.matcha_btn, self.chocolate_btn]:
            btn.setStyleSheet("")

        for btn in [self.reg_btn, self.lrg_btn]:
            btn.setStyleSheet("")
            btn.setEnabled(False)

        for btn in [self.hot_btn, self.cold_btn]:
            btn.setStyleSheet("")
            btn.setEnabled(False)

        self.qty_input.setText("1")
        self.add_btn.setEnabled(False)

    def process_payment(self):
        try:
            amount_received = float(self.amount_input.text())
            total = sum(self.pay_amounts)

            if amount_received < total:
                QMessageBox.warning(self, "Insufficient Payment", "Amount received is less than total.")
                return

            change = amount_received - total

            self.received_label.setText(f"RECEIVED: ₱{amount_received}")
            self.change_label.setText(f"CHANGE: ₱{change}")

            # Save to CSV (optional)
            self.save_order_to_csv(total, amount_received, change)

            # Show receipt dialog
            self.show_receipt(total, amount_received, change)

        except ValueError:
            QMessageBox.warning(self, "Invalid Input", "Please enter a valid amount.")

    def show_receipt(self, total, received, change):
        customer_name = self.customer_input.text()
        receipt_dialog = ReceiptDialog(customer_name, self.order_items, total, received, change, self)
        receipt_dialog.back_clicked.connect(self.handle_receipt_back)
        receipt_dialog.exec_()

    def handle_receipt_back(self):
        """Handle when user clicks back from receipt - save order and continue"""
        # Create previous order item
        customer_name = self.customer_input.text() or "Customer"
        order_items_text = []
        for item in self.order_items:
            order_items_text.append(f"{item.order_name}{item.order_qty}")

        previous_order = PreviousOrderItem(customer_name, order_items_text)
        self.previous_list_layout.addWidget(previous_order)

        # Clear current order but keep UI ready for next order
        for item in self.order_items:
            self.order_list_layout.removeWidget(item)
            item.deleteLater()

        self.order_items.clear()
        self.pay_amounts.clear()

        # Reset UI for next order
        self.customer_input.clear()
        self.amount_input.clear()
        self.total_label.setText("TOTAL: ₱0")
        self.received_label.setText("RECEIVED: ₱0")
        self.change_label.setText("CHANGE: ₱0")

        self.reset_selection()

        # Show message that order was saved
        QMessageBox.information(self, "Order Saved",
                                "Order has been saved to previous orders. You can continue with a new order.")

    def save_order_to_csv(self, total, received, change):
        try:
            order_summary = []
            for item in self.order_items:
                order_summary.append(f"{item.order_name}{item.order_qty}")

            data = {
                'timestamp': datetime.now(),
                'orders': str(order_summary),
                'total': total,
                'received': received,
                'change': change,
                'customer': self.customer_input.text()
            }

            # Save to CSV file
            df = pd.DataFrame([data])
            df.to_csv('order-summary.csv', mode='a', header=False, index=False)

        except Exception as e:
            print(f"Error saving to CSV: {e}")

    def next_order(self):
        # Create previous order item
        customer_name = self.customer_input.text() or "Customer"
        order_items_text = []
        for item in self.order_items:
            order_items_text.append(f"{item.order_name}{item.order_qty}")

        previous_order = PreviousOrderItem(customer_name, order_items_text)
        self.previous_list_layout.addWidget(previous_order)

        # Clear current order
        for item in self.order_items:
            self.order_list_layout.removeWidget(item)
            item.deleteLater()

        self.order_items.clear()
        self.pay_amounts.clear()

        # Reset UI
        self.customer_input.clear()
        self.amount_input.clear()
        self.total_label.setText("TOTAL: ₱0")
        self.received_label.setText("RECEIVED: ₱0")
        self.change_label.setText("CHANGE: ₱0")
        self.next_order_btn.setEnabled(False)

        self.reset_selection()


def main():
    app = QApplication(sys.argv)
    app.setStyle('Fusion')
    window = CafePOSApp()
    window.show()
    sys.exit(app.exec_())


if __name__ == '__main__':
    main()