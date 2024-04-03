# Generated by Django 4.2 on 2023-04-25 16:29

from django.conf import settings
from django.db import migrations, models
import django.db.models.deletion


class Migration(migrations.Migration):

    initial = True

    dependencies = [
        ('auth', '0012_alter_user_first_name_max_length'),
    ]

    operations = [
        migrations.CreateModel(
            name='UserAccount',
            fields=[
                ('user', models.OneToOneField(on_delete=django.db.models.deletion.CASCADE, primary_key=True, serialize=False, to=settings.AUTH_USER_MODEL)),
                ('phone', models.BigIntegerField(default=0)),
                ('creditCard', models.BigIntegerField(default=0)),
                ('ups_username', models.CharField(blank=True, default='', max_length=50)),
                ('ups_userid', models.IntegerField(default=-1)),
            ],
        ),
        migrations.CreateModel(
            name='Address',
            fields=[
                ('id', models.BigAutoField(auto_created=True, primary_key=True, serialize=False, verbose_name='ID')),
                ('addressName', models.CharField(default='', max_length=200)),
                ('address_x', models.IntegerField(blank=True, default=-1)),
                ('address_y', models.IntegerField(blank=True, default=-1)),
                ('owner', models.ForeignKey(on_delete=django.db.models.deletion.CASCADE, related_name='addresses', to='amazonAccount.useraccount')),
            ],
        ),
    ]
