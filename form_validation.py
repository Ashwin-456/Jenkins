username = input("Enter username: ")
password = input("Enter password: ")

if len(username) < 3:
    print("Username too short")
elif len(password) < 6:
    print("Password too weak")
else:
    print("Form submitted successfully")
