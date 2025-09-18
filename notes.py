notes = []

print("Notes App")

while True:
    print("\n1. Add Note\n2. View Notes\n3. Quit")
    choice = input("Choose: ")

    if choice == "1":
        note = input("Enter note: ")
        notes.append(note)
    elif choice == "2":
        print("\nYour Notes:")
        for n in notes:
            print("-", n)
    elif choice == "3":
        break
    else:
        print("Invalid choice")
