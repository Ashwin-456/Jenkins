expenses = []

print("Expense Tracker")

while True:
    print("\n1. Add Expense\n2. View Expenses\n3. Quit")
    choice = input("Choose: ")

    if choice == "1":
        item = input("Enter item: ")
        amount = float(input("Enter amount: "))
        expenses.append((item, amount))
        print("Added:", item, "-", amount)

    elif choice == "2":
        print("\nExpenses:")
        total = 0
        for e in expenses:
            print(e[0], "-", e[1])
            total += e[1]
        print("Total:", total)

    elif choice == "3":
        break
    else:
        print("Invalid choice")
