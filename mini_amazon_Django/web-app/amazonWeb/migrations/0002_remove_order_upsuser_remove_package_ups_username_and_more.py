# Generated by Django 4.1.5 on 2023-04-27 22:27

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('amazonWeb', '0001_initial'),
    ]

    operations = [
        migrations.RemoveField(
            model_name='order',
            name='upsUser',
        ),
        migrations.RemoveField(
            model_name='package',
            name='ups_username',
        ),
        migrations.AlterField(
            model_name='order',
            name='shipID',
            field=models.BigAutoField(primary_key=True, serialize=False),
        ),
    ]
