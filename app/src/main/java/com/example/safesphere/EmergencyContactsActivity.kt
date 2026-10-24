package com.example.safesphere

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.safesphere.data.Contact
import com.example.safesphere.data.ContactRepository
import com.example.safesphere.ui.theme.*
import kotlinx.coroutines.launch

class EmergencyContactsActivity : ComponentActivity() {
    private val repository = ContactRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SAfeSphereTheme {
                val contacts by repository.getContacts().collectAsState(initial = emptyList())
                var showDialog by remember { mutableStateOf<Contact?>(null) }
                var isAdding by remember { mutableStateOf(false) }

                Box(modifier = Modifier.fillMaxSize().background(BackgroundGradient)) {
                    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { finish() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Text("Emergency Contacts", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(contacts) { contact ->
                                ContactCard(
                                    contact = contact,
                                    onEdit = { showDialog = contact },
                                    onDelete = {
                                        lifecycleScope.launch {
                                            if (repository.deleteContact(contact.id)) {
                                                Toast.makeText(this@EmergencyContactsActivity, "Contact Deleted", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        FloatingActionButton(
                            onClick = { isAdding = true },
                            containerColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.End).padding(bottom = 16.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                    }

                    if (isAdding || showDialog != null) {
                        ContactDialog(
                            contact = showDialog,
                            onDismiss = { 
                                isAdding = false
                                showDialog = null
                            },
                            onSave = { name, phone, rel, isPrimary ->
                                // Close dialog immediately
                                val currentContact = showDialog
                                isAdding = false
                                showDialog = null
                                
                                lifecycleScope.launch {
                                    val success = if (currentContact != null) {
                                        repository.updateContact(currentContact.copy(name = name, phone = phone, relationship = rel, isPrimary = isPrimary))
                                    } else {
                                        repository.addContact(Contact(name = name, phone = phone, relationship = rel, isPrimary = isPrimary))
                                    }
                                    if (success) {
                                        Toast.makeText(this@EmergencyContactsActivity, "Contact Saved", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this@EmergencyContactsActivity, "Failed to save contact", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContactCard(contact: Contact, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = if (contact.isPrimary) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (contact.isPrimary) Icons.Default.Star else Icons.Default.Person,
                        contentDescription = null,
                        tint = if (contact.isPrimary) ElectricBlue else MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(contact.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    if (contact.isPrimary) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(color = ElectricBlue.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
                            Text("Primary", color = ElectricBlue, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 4.dp))
                        }
                    }
                }
                Text(contact.phone, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                if (contact.relationship.isNotEmpty()) {
                    Text(contact.relationship, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray) }
        }
    }
}

@Composable
fun ContactDialog(contact: Contact?, onDismiss: () -> Unit, onSave: (String, String, String, Boolean) -> Unit) {
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var phone by remember { mutableStateOf(contact?.phone ?: "") }
    var rel by remember { mutableStateOf(contact?.relationship ?: "") }
    var isPrimary by remember { mutableStateOf(contact?.isPrimary ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (contact != null) "Edit Contact" else "Add Contact") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                TextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                TextField(value = rel, onValueChange = { rel = it }, label = { Text("Relationship (Optional)") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPrimary, onCheckedChange = { isPrimary = it })
                    Text("Mark as Primary Contact", color = Color.White)
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank() && phone.isNotBlank()) onSave(name, phone, rel, isPrimary) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Discard") }
        }
    )
}
