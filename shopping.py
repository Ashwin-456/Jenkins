cart = []

print("Online Shopping")

while True:
    print("\n1. Add Item\n2. View Cart\n3. Checkout\n4. Quit")
    choice = input("Choose: ")

    if choice == "1":
        item = input("Enter item: ")
        price = float(input("Enter price: "))
        cart.append((item, price))
    elif choice == "2":
        print("\nYour Cart:")
        total = 0
        for c in cart:
            print(c[0], "-", c[1])
            total += c[1]
        print("Total:", total)
    elif choice == "3":
        print("Checkout complete. Total amount:", sum([c[1] for c in cart]))
        cart.clear()
    elif choice == "4":
        break
    else:
        print("Invalid choice")
