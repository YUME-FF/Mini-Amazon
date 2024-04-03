from django.shortcuts import render, redirect
# Create your views here.
from django.http import HttpResponse
from django.contrib import messages
from django import forms
from django.contrib.auth import login as django_login
from django.contrib.auth import logout as django_logout
from django.contrib.auth.forms import UserCreationForm
from django.contrib.auth import authenticate, logout, login
from .models import *
from django.contrib.auth.decorators import login_required
from .forms import *
from .models import *
from amazonWeb.helper import *


def home(request):
    return render(request, 'amazonAccount/home.html')


def login(request):
    if request.method == 'POST':
        username = request.POST.get('username').lower()
        password = request.POST.get('password')

        user = authenticate(request, username=username, password=password)

        if user is not None:
            useraccount = UserAccount(user=user)
            useraccount.save()
            django_login(request, user)
            return redirect('UserHome')
        else:
            messages.error(request, 'Username or passowrd is not correct')
    return render(request, 'amazonAccount/login.html', {'page': 'login'})


def logout(request):
    django_logout(request)
    return redirect('home')


def createAccount(request):
    form = UserCreationForm()
    if request.method == 'POST':
        form = UserCreationForm(request.POST)
        if form.is_valid():
            user = form.save(commit=False)
            user.save()
            # get the raw password
            raw_password = form.cleaned_data.get('password1')
            userinfo = str(user.id) + " " + user.username + " " + raw_password
            print(userinfo)
            checkUPSuserinfo(userinfo)
            useraccount = UserAccount(user=user)
            useraccount.save()
            return redirect('home')
        else:
            messages.error(request, 'Please enter correct format')
    return render(request, 'amazonAccount/login.html', {'form': form})


@login_required
def profile(request):
    user = request.user
    context = {}
    context["username"] = user.username
    context["email"] = user.email
    userAccount = user.useraccount
    context["useraccount"] = userAccount
    not_initialized = (userAccount.phone == 0 and userAccount.creditCard ==
                       0)
    context["useraccount_initialized"] = None if not_initialized else True
    context["addresses"] = userAccount.addresses.all()
    return render(request, 'amazonAccount/profile.html', context)


@login_required
def editProfile(request):
    curr_user = request.user
    if request.method == 'POST':
        form = EditProfileForm(request.POST)
        if form.is_valid():
            curr_user.email = form.cleaned_data['email']
            curr_user.username = form.cleaned_data['username']
            curr_user.save()
            messages.success(request, f'Your profile has been updated!')
            return redirect('profile')
        else:
            form = EditProfileForm()
            return render(request, 'amazonAccount/editProfile.html', {'form': form})
    else:
        form = EditProfileForm()
        return render(request, 'amazonAccount/editProfile.html', {'form': form})


@login_required
def editDeliveryProfile(request):
    curr_user = request.user
    if request.method == 'POST':
        form = EditDeliveryProfileForm(request.POST)
        if form.is_valid():
            curr_user.useraccount.phone = form.cleaned_data['phone']
            curr_user.useraccount.creditCard = form.cleaned_data['creditCard']
            curr_user.useraccount.save()
            messages.success(
                request, f'Your Delivery profile has been updated!')
            return redirect('profile')
        else:
            form = EditDeliveryProfileForm()
            return render(request, 'amazonAccount/editDeliveryProfile.html', {'form': form})
    else:
        form = EditDeliveryProfileForm()
        return render(request, 'amazonAccount/editDeliveryProfile.html', {'form': form})


@login_required
def editDeliveryAddress(request):
    return render(request, 'amazonAccount/editDeliveryAddress.html')


@login_required
def addDeliveryAddress(request):
    curr_user_account = request.user.useraccount
    if request.method == 'POST':
        form = addDeliveryAddressForm(request.POST)
        if form.is_valid():
            name = form.cleaned_data['addressName']
            address_x = form.cleaned_data['address_x']
            address_y = form.cleaned_data['address_y']
            address = Address(owner=curr_user_account, addressName=name,
                              address_x=address_x, address_y=address_y)
            address.save()
            messages.success(
                request, f'Your new delivery address has been added!')
            return redirect('profile')
        else:
            form = addDeliveryAddressForm()
            return render(request, 'amazonAccount/addDeliveryAddress.html', {'form': form})
    else:
        form = addDeliveryAddressForm()
        return render(request, 'amazonAccount/addDeliveryAddress.html', {'form': form})
