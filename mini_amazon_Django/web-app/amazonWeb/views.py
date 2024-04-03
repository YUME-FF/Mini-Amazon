from django.shortcuts import render
from .models import *
from django.contrib.auth.decorators import login_required
from django.contrib import messages
from django.shortcuts import redirect
from .forms import *
from django.core.mail import send_mail
from django.conf import settings
from .helper import sendToBackend as notify_backend

# Create your views here.


class myProduct:
    def __init__(self, ID, description, category, price):
        self.id = ID
        self.description = description
        self.category = category
        self.price = price


def userHome(request):
    context = {}
    context["category"] = "all"
    all_products = Product.objects.all()
    myProducts = []
    for product in all_products:
        cat = Category.objects.filter(id=product.category_id).first().category
        myProducts.append(
            myProduct(product.id, product.description, cat, product.price))
    context["products"] = myProducts
    context["categories"] = Category.objects.all()
    return render(request, 'amazonWeb/UserHome.html', context)


def myCategory(request, category_id):
    context = {}
    if (category_id == -1):
        context["category"] = "all"
        all_products = Product.objects.all()
    else:
        cat = Category.objects.filter(id=category_id).first()
        context["category"] = cat.category
        all_products = cat.products.all()
    myProducts = []
    for product in all_products:
        cat = Category.objects.filter(id=product.category_id).first().category
        myProducts.append(myProduct(
            product.id, product.description, cat, product.price))
    context["products"] = myProducts
    context["categories"] = Category.objects.all()
    return render(request, 'amazonWeb/UserHome.html', context)


class myOrder:
    def __init__(self, order, shipID, truckID, productDescription, productCount, status, locationX, locationY):
        self.order = order
        self.shipID = shipID
        self.truckID = truckID
        self.productDescription = productDescription
        self.productCount = productCount
        self.status = status
        self.locationX = locationX
        self.locationY = locationY


@login_required
def orderHistory(request):
    context = {}
    user = request.user
    orders = Order.objects.filter(userID=user.id)
    combines = []
    for order in orders:
        shipID = order.shipID
        productDescription = order.productDescription
        productCount = order.productCount
        status = order.status
        locationX = order.locationX
        locationY = order.locationY
        if order.truckID == 0:
            truckID = "not generated yet"
        else:
            truckID = order.truckID
        combines.append(myOrder(order, shipID, truckID,
                        productDescription, productCount, status, locationX, locationY))
    context["orderHistory"] = combines
    return render(request, 'amazonWeb/myorders.html', context)


@login_required
def purchase(request, product_id):
    user = request.user
    if request.method == 'POST':
        form = PurchaseForm(request.POST)
        if form.is_valid():
            address_x = form.cleaned_data["address_x"]
            address_y = form.cleaned_data["address_y"]
            quantity = form.cleaned_data["quantity"]
            product = Product.objects.filter(id=product_id).first()
            package = Package(customer=user, address_x=address_x,
                              address_y=address_y)
            package.save()
            order = Order2(customer=user, product=product,
                           package=package, quantity=quantity)
            order.save()

            user_ID = user.id
            status = "processing"
            order2 = Order(
                whID=0,
                truckID=0,
                locationX=address_x,
                locationY=address_y,
                productID=product.id,
                productDescription=product.description,
                productCount=quantity,
                status=status,
                userID=user_ID,
            )
            order2.save()

            result = notify_backend(order2.shipID)
            if result:
                subject = "Your order has been placed!"
                content = f'You have ordered {quantity} {product.description}\n'
                content += f'Delivering to {address_x}, {address_y}\n'
                content += "Best,\nMini Amazon Team\n"
                from_email = settings.EMAIL_HOST_USER
                email_list = [user.email]
                send_mail(subject, content, from_email,
                          email_list, fail_silently=False)
                messages.success(
                    request, f'You have successfully put the order!')
                print("success send email to " + user.email)
                print(content)
                return redirect('UserHome')
            else:
                package.delete()
                order.delete()
                messages.warning(
                    request, f'Something failed! Please try again!')
                return redirect('UserHome')
        else:
            form = PurchaseForm()
            return render(request, 'amazonWeb/purchase.html', {'form': form, 'product_id': product_id})
    else:
        form = PurchaseForm()
        return render(request, 'amazonWeb/purchase.html', {'form': form, 'product_id': product_id})
