import sys
from PyQt5.QtWidgets import (
    QApplication, QWidget, QLabel, QLineEdit, QPushButton, QVBoxLayout, QMessageBox
)
from PyQt5.QtCore import Qt
from PyQt5.QtGui import QFont

# Import the main window class from main.py
from main import CafePOSApp

users = {
    "admin": "password123",
    "student": "cosc90"
}


class LoginWindow(QWidget):
    def __init__(self):
        super().__init__()
        self.setWindowTitle("Cafe Login")
        self.setGeometry(100, 100, 600, 500)
        self.setFixedSize(600, 600)
        self.main_window = None  # Store reference to main window

        # Set the main window style
        self.setStyleSheet("""
            QWidget {
                background: qlineargradient(x1:0, y1:0, x2:0, y2:1,
                    stop:0 #8B4513, stop:0.5 #A0522D, stop:1 #654321);
                color: #F5DEB3;
                font-family: 'Arial', sans-serif;
            }
        """)

        layout = QVBoxLayout()
        layout.setSpacing(30)
        layout.setContentsMargins(60, 60, 60, 60)

        # Title Label
        self.title_label = QLabel("☕ CAFE LOGIN ☕")
        self.title_label.setAlignment(Qt.AlignCenter)
        self.title_label.setStyleSheet("""
            QLabel {
                font-size: 30px;
                font-weight: bold;
                color: #F5DEB3;
                background: transparent;
                padding: 30px;
                border-radius: 15px;
                margin-bottom: 40px;
            }
        """)

        # Username Label
        self.label_username = QLabel("Username:")
        self.label_username.setStyleSheet("""
            QLabel {
                font-size: 18px;
                font-weight: bold;
                color: #F5DEB3;
                background: transparent;
                padding: 8px;
            }
        """)

        # Username Input
        self.textbox_username = QLineEdit()
        self.textbox_username.setStyleSheet("""
            QLineEdit {
                background-color: #F5DEB3;
                border: 3px solid #8B4513;
                border-radius: 12px;
                padding: 18px;
                font-size: 16px;
                color: #4A4A4A;
                min-height: 20px;
            }
            QLineEdit:focus {
                border: 3px solid #D2691E;
                background-color: #FFFAF0;
            }
        """)

        # Password Label
        self.label_password = QLabel("Password:")
        self.label_password.setStyleSheet("""
            QLabel {
                font-size: 18px;
                font-weight: bold;
                color: #F5DEB3;
                background: transparent;
                padding: 8px;
            }
        """)

        # Password Input
        self.textbox_password = QLineEdit()
        self.textbox_password.setEchoMode(QLineEdit.Password)
        self.textbox_password.setStyleSheet("""
            QLineEdit {
                background-color: #F5DEB3;
                border: 3px solid #8B4513;
                border-radius: 12px;
                padding: 18px;
                font-size: 16px;
                color: #4A4A4A;
                min-height: 20px;
            }
            QLineEdit:focus {
                border: 3px solid #D2691E;
                background-color: #FFFAF0;
            }
        """)

        # Login Button
        self.button_login = QPushButton("☕ LOGIN")
        self.button_login.clicked.connect(self.check_login)
        self.button_login.setStyleSheet("""
            QPushButton {
                background: qlineargradient(x1:0, y1:0, x2:0, y2:1,
                    stop:0 #D2691E, stop:1 #8B4513);
                border: none;
                border-radius: 18px;
                color: white;
                font-size: 20px;
                font-weight: bold;
                padding: 25px;
                margin-top: 20px;
                min-height: 30px;
            }
            QPushButton:hover {
                background: qlineargradient(x1:0, y1:0, x2:0, y2:1,
                    stop:0 #FF8C00, stop:1 #D2691E);
                transform: translateY(-2px);
            }
            QPushButton:pressed {
                background: qlineargradient(x1:0, y1:0, x2:0, y2:1,
                    stop:0 #8B4513, stop:1 #654321);
            }
        """)

        # Add widgets to layout
        layout.addWidget(self.title_label)
        layout.addWidget(self.label_username)
        layout.addWidget(self.textbox_username)
        layout.addWidget(self.label_password)
        layout.addWidget(self.textbox_password)
        layout.addWidget(self.button_login)

        self.setLayout(layout)

    def check_login(self):
        username = self.textbox_username.text()
        password = self.textbox_password.text()

        if username in users and users[username] == password:
            # Custom styled success message
            msg = QMessageBox()
            msg.setIcon(QMessageBox.Information)
            msg.setWindowTitle("Login Successful")
            msg.setText(f"☕ Welcome {username}! ☕\n\nEnjoy your cafe experience!")
            msg.setStyleSheet("""
                QMessageBox {
                    background-color: #F5DEB3;
                    color: #8B4513;
                    font-size: 14px;
                }
                QMessageBox QPushButton {
                    background-color: #D2691E;
                    color: white;
                    border: none;
                    border-radius: 8px;
                    padding: 12px 25px;
                    font-weight: bold;
                    font-size: 14px;
                }
                QMessageBox QPushButton:hover {
                    background-color: #FF8C00;
                }
            """)
            msg.exec_()

            # Hide login window and show main window
            self.hide()
            self.main_window = CafePOSApp()
            self.main_window.show()
        else:
            # Custom styled error message
            msg = QMessageBox()
            msg.setIcon(QMessageBox.Warning)
            msg.setWindowTitle("Login Failed")
            msg.setText("❌ Incorrect username or password.\n\nPlease try again!")
            msg.setStyleSheet("""
                QMessageBox {
                    background-color: #F5DEB3;
                    color: #8B4513;
                    font-size: 14px;
                }
                QMessageBox QPushButton {
                    background-color: #CD5C5C;
                    color: white;
                    border: none;
                    border-radius: 8px;
                    padding: 12px 25px;
                    font-weight: bold;
                    font-size: 14px;
                }
                QMessageBox QPushButton:hover {
                    background-color: #DC143C;
                }
            """)
            msg.exec_()


if __name__ == "__main__":
    app = QApplication(sys.argv)
    window = LoginWindow()
    window.show()
    sys.exit(app.exec_())